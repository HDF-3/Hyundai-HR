package humanresource.service;

import humanresource.dao.DepartmentDAO;
import humanresource.dto.DepartmentDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DepartmentService {
    private final DepartmentDAO departmentDAO;

    public DepartmentService() {
        this.departmentDAO = new DepartmentDAO();
    }

    public int registerDepartment(DepartmentDTO departmentDTO) {
        return departmentDAO.insertDepartment(departmentDTO);
    }

    public int removeDepartment(Long deptId) {
        int employeeCount = getEmployeeCount(deptId);
        if (employeeCount > 0) {
            throw new IllegalStateException("부서에 소속된 직원이 존재하여 삭제할 수 없습니다. (직원 수: " + employeeCount + ")");
        }
        return departmentDAO.deleteDepartment(deptId);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentDAO.selectAllDepartment();
    }

    public int getEmployeeCount(Long deptId) {
        return departmentDAO.countEmployeesInDept(deptId);
    }

    // =========================================================================
    // 조직도 관련 메서드
    // =========================================================================

    /**
     * 최상위 부서 목록 조회 (parentDeptId가 null인 부서)
     */
    public List<DepartmentDTO> getRootDepartments() {
        return getAllDepartments().stream()
                .filter(d -> d.getParentDeptId() == null)
                .collect(Collectors.toList());
    }

    /**
     * 특정 부서의 직속 하위 부서 목록 조회
     */
    public List<DepartmentDTO> getChildDepartments(Long parentDeptId) {
        if (parentDeptId == null)
            return new ArrayList<>();
        return getAllDepartments().stream()
                .filter(d -> parentDeptId.equals(d.getParentDeptId()))
                .collect(Collectors.toList());
    }

    /**
     * 조직도 트리 구조 반환
     * Key: 부서ID, Value: 해당 부서의 하위 부서 목록
     */
    public Map<Long, List<DepartmentDTO>> getOrganizationTree() {
        List<DepartmentDTO> allDepts = getAllDepartments();

        // parentDeptId 기준으로 그룹핑
        Map<Long, List<DepartmentDTO>> childrenMap = allDepts.stream()
                .filter(d -> d.getParentDeptId() != null)
                .collect(Collectors.groupingBy(DepartmentDTO::getParentDeptId));

        return childrenMap;
    }

    /**
     * 특정 부서의 모든 하위 부서를 재귀적으로 조회 (자기 자신 포함)
     */
    public List<DepartmentDTO> getAllSubDepartments(Long deptId) {
        List<DepartmentDTO> allDepts = getAllDepartments();
        Map<Long, List<DepartmentDTO>> childrenMap = allDepts.stream()
                .filter(d -> d.getParentDeptId() != null)
                .collect(Collectors.groupingBy(DepartmentDTO::getParentDeptId));

        List<DepartmentDTO> result = new ArrayList<>();
        DepartmentDTO root = allDepts.stream()
                .filter(d -> d.getDeptId().equals(deptId))
                .findFirst().orElse(null);

        if (root != null) {
            collectSubDepts(root, childrenMap, result);
        }
        return result;
    }

    private void collectSubDepts(DepartmentDTO dept, Map<Long, List<DepartmentDTO>> childrenMap,
            List<DepartmentDTO> result) {
        result.add(dept);
        List<DepartmentDTO> children = childrenMap.getOrDefault(dept.getDeptId(), new ArrayList<>());
        for (DepartmentDTO child : children) {
            collectSubDepts(child, childrenMap, result);
        }
    }

}