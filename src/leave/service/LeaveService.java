package leave.service;

import global.types.CommonStatus;
import global.types.DBType;
import global.utils.ConnectionHelper;
import leave.dao.LeaveApprovalDAO;
import leave.dao.LeaveDAO;
import leave.dto.AnnualLeaveDTO;
import leave.dto.LeaveRequestDTO;
import leave.factory.LeaveFactory;
import leave.strategy.LeavePolicy;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class LeaveService {
    private LeaveDAO leaveDAO = new LeaveDAO();
    private LeaveApprovalDAO leaveApprovalDAO = new LeaveApprovalDAO();

    // 개인 연차 목록 조회
    public void getAnnualLeaveList(Long employeeId) {
        List<AnnualLeaveDTO> list = leaveDAO.findAnnualLeaveListByEmployeeId(employeeId);

        for (AnnualLeaveDTO annualLeaveDTO : list) {
            printAnnualLeave(annualLeaveDTO);
        }
    }
    // 현재년도(활성화 = Y) 연차 조회
    public void getAnnualLeave(Long employeeId) {
        AnnualLeaveDTO leaveDTO = leaveDAO.findAnnualLeaveById(employeeId);

        printAnnualLeave(leaveDTO);
    }
    // 휴가 신청
    public boolean requestLeave(LeaveRequestDTO leaveRequestDTO) {
        try {
            Long employeeId = leaveRequestDTO.getEmployeeId();

            if (leaveDAO.isDuplicateLeaveRequest(employeeId, leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate())) {
                System.out.println("휴가 신청 실패: 해당 기간에 이미 신청되었거나 승인된 휴가가 존재합니다.");
                return false;
            }

            double remainingAnnualLeave = leaveDAO.findAnnualLeaveById(employeeId).getRemainingAnnualLeave();

            LeavePolicy leave = LeaveFactory.getLeave(leaveRequestDTO.getLeaveType());
            double requiredAnnualLeave = leave.calculateDeduction(leaveRequestDTO.getStartDate(), leaveRequestDTO.getEndDate());

            if (remainingAnnualLeave < requiredAnnualLeave) {
                System.out.println("잔여 연차가 부족합니다. (남은 연차: " + remainingAnnualLeave + ")");
                return false;
            }

            boolean result = leaveDAO.insertLeaveRequest(leaveRequestDTO);
            if (result) {
                System.out.println("휴가 신청 성공 (결재 대기 중)");
            } else {
                System.out.println("휴가 신청 시스템 오류");
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
    // 신청했었던 휴가 리스트 조회
    public void getLeaveRequestList(Long employeeId) {
        List<LeaveRequestDTO> list = leaveDAO.findLeaveRequestsByEmployeeId(employeeId);

        if (list.isEmpty()) {
            System.out.println("신청한 휴가 내역이 존재하지 않습니다.");
            return;
        }

        System.out.println("[" + employeeId + "]번 사원의 휴가 신청 내역");
        for (LeaveRequestDTO leaveRequestDTO : list) {
            printLeaveApproval(leaveRequestDTO);
        }
    }
    // 휴가 취소
    public boolean cancelLeave(Long leaveRequestId) {
        boolean result = leaveDAO.cancelLeaveRequest(leaveRequestId);

        if (result) {
            System.out.println("휴가 신청이 성공적으로 취소되었습니다.");
        } else {
            System.out.println("휴가 취소 실패 (이미 처리된 결재건이거나 존재하지 않는 신청 번호입니다.)");
        }
        return result;
    }
    // (관리자) 같은 부서 직원들의 대기 중인 휴가 목록 조회
    public void getLeaveApprovalList(Long adminId) {
        List<LeaveRequestDTO> list = leaveApprovalDAO.findPendingRequest(adminId);
        if (list.isEmpty()) {
            System.out.println("결재 대기 중인 휴가 신청이 없습니다.");
            return;
        }
        for (LeaveRequestDTO leaveRequestDTO : list) {
            printLeaveApproval(leaveRequestDTO);
        }
    }
    // (관리자) 휴가 승인 & 휴가 정책에 따른 연차 차감 완성
    public void processApproval(Long adminId, Long leaveRequestId, CommonStatus status) {
        Connection conn = null;
        try {
            conn = ConnectionHelper.getConnection(DBType.ORACLE);
            conn.setAutoCommit(false);

            if (!leaveApprovalDAO.isAdmin(conn, adminId)) {
                throw new RuntimeException("관리자 권한이 없습니다.");
            }

            LeaveRequestDTO request = leaveApprovalDAO.getLeaveRequestById(conn, leaveRequestId);
            if (request == null) {
                throw new RuntimeException("존재하지 않는 휴가 신청건입니다.");
            }

            if (request.getStatus() != CommonStatus.PENDING) {
                throw new RuntimeException("이미 처리 완료되었거나 취소된 휴가 신청건입니다.");
            }

            boolean statusResult = leaveApprovalDAO.updateLeaveRequestStatus(conn, request.getLeaveRequestId(), status);
            if (!statusResult) {
                throw new RuntimeException("휴가 상태 업데이트에 실패했습니다.");
            }

            if (status == CommonStatus.APPROVED) {
                LeavePolicy policy = LeaveFactory.getLeave(request.getLeaveType());
                double deductionDays = policy.calculateDeduction(request.getStartDate(), request.getEndDate());

                if (deductionDays > 0) {
                    boolean deductResult = leaveApprovalDAO.deductAnnualLeave(conn, request.getEmployeeId(), deductionDays);
                    if (!deductResult) {
                        throw new RuntimeException("연차 차감에 실패했습니다.");
                    }
                    System.out.println("휴가 승인 및 연차 " + deductionDays + "일 차감 완료");
                } else {
                    System.out.println("휴가 승인 완료 (연차 차감 없음 - 외근/공가 등)");
                }
            } else if (status == CommonStatus.REJECTED) {
                System.out.println("휴가 신청이 반려되었습니다.");
            }
            else {
                throw new IllegalArgumentException("잘못된 결재 상태값입니다. 승인(APPROVED) 또는 반려(REJECTED)만 처리 가능합니다. 입력값: " + status);
            }

            boolean logResult = leaveApprovalDAO.insertLeaveApprovalLog(conn, adminId, leaveRequestId);
            if (!logResult) {
                throw new RuntimeException("결재 이력 기록에 실패했습니다.");
            }

            // 예외 없이 성공적으로 완수되었을 때만 최종 커밋
            conn.commit();
            System.out.println("결재 처리가 성공적으로 커밋되었습니다.");

        } catch (Exception e) {
            System.out.println("결재 처리 중 오류 발생으로 인해 롤백합니다: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            // 성공하든 실패하든 Connection 자원은 무조건 반납
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void printAnnualLeave(AnnualLeaveDTO annualLeaveDTO) {
        System.out.println("부여된 휴가 : " + annualLeaveDTO.getGrantedAnnualLeave());
        System.out.println("사용된 휴가 : " + annualLeaveDTO.getUsedAnnualLeave());
        System.out.println("남은 휴가 : " + annualLeaveDTO.getRemainingAnnualLeave());
        System.out.println("생성일 : " + annualLeaveDTO.getGrantedAt());
        System.out.println("만료일 : " + annualLeaveDTO.getExpiredAt());
    }
    public void printLeaveApproval(LeaveRequestDTO leaveRequestDTO) {
        System.out.println("휴가 신청 번호 : " + leaveRequestDTO.getLeaveRequestId());
        System.out.println("사번 : " + leaveRequestDTO.getEmployeeId());
        System.out.println("휴가 사유 : " + leaveRequestDTO.getReason());
        System.out.println("시작일 : " + leaveRequestDTO.getStartDate());
        System.out.println("종료일 : " + leaveRequestDTO.getEndDate());
        System.out.println("휴가 유형 : " + leaveRequestDTO.getLeaveType());
        System.out.println("신청 상태 : " + leaveRequestDTO.getStatus());
    }

}
