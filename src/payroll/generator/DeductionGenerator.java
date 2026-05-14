package payroll.generator;

import java.math.BigDecimal;
import java.time.YearMonth;

import payroll.dto.DeductionDTO;

public class DeductionGenerator {

    private static final BigDecimal NATIONAL_PENSION_RATE = new BigDecimal("0.045");
    private static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.03545");
    private static final BigDecimal LONG_TERM_CARE_INSURANCE_RATE = new BigDecimal("0.1295");
    private static final BigDecimal EMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.009");
    private static final BigDecimal LOCAL_INCOME_TAX_RATE = new BigDecimal("0.10");

    private static final BigDecimal INCOME_TAX_FIRST_LIMIT = new BigDecimal("3000000");
    private static final BigDecimal INCOME_TAX_SECOND_LIMIT = new BigDecimal("5000000");
    private static final BigDecimal INCOME_TAX_FIRST_RATE = new BigDecimal("0.03");
    private static final BigDecimal INCOME_TAX_SECOND_RATE = new BigDecimal("0.06");
    private static final BigDecimal INCOME_TAX_THIRD_RATE = new BigDecimal("0.10");

    public DeductionDTO generate(
    	Long deductionId,
        Long payrollId,
        YearMonth yearMonth,
        BigDecimal totalEarnings
    ) {
        BigDecimal incomeTax = calculateIncomeTax(totalEarnings);
        BigDecimal healthInsurance = calculateHealthInsurance(totalEarnings);

        DeductionDTO deduction = new DeductionDTO();

        deduction.setDeductionId(deductionId);
        deduction.setPayrollId(payrollId);
        deduction.setNationalPension(calculateNationalPension(totalEarnings));
        deduction.setHealthInsurance(healthInsurance);
        deduction.setLongTermCareInsurance(calculateLongTermCareInsurance(healthInsurance));
        deduction.setEmploymentInsurance(calculateEmploymentInsurance(totalEarnings));
        deduction.setIncomeTax(incomeTax);
        deduction.setLocalIncomeTax(calculateLocalIncomeTax(incomeTax));

        return deduction;
    }

    private BigDecimal calculateNationalPension(BigDecimal totalEarnings) {
        return calculateByRate(totalEarnings, NATIONAL_PENSION_RATE);
    }

    private BigDecimal calculateHealthInsurance(BigDecimal totalEarnings) {
        return calculateByRate(totalEarnings, HEALTH_INSURANCE_RATE);
    }

    private BigDecimal calculateLongTermCareInsurance(BigDecimal healthInsurance) {
        return calculateByRate(healthInsurance, LONG_TERM_CARE_INSURANCE_RATE);
    }

    private BigDecimal calculateEmploymentInsurance(BigDecimal totalEarnings) {
        return calculateByRate(totalEarnings, EMPLOYMENT_INSURANCE_RATE);
    }

    private BigDecimal calculateIncomeTax(BigDecimal totalEarnings) {
        BigDecimal tax = BigDecimal.ZERO;

        if (totalEarnings.compareTo(INCOME_TAX_FIRST_LIMIT) <= 0) {
            return totalEarnings.multiply(INCOME_TAX_FIRST_RATE);
        }

        tax = tax.add(INCOME_TAX_FIRST_LIMIT.multiply(INCOME_TAX_FIRST_RATE));

        if (totalEarnings.compareTo(INCOME_TAX_SECOND_LIMIT) <= 0) {
            BigDecimal secondSection = totalEarnings.subtract(INCOME_TAX_FIRST_LIMIT);
            return tax.add(secondSection.multiply(INCOME_TAX_SECOND_RATE));
        }

        tax = tax.add(
        	INCOME_TAX_SECOND_LIMIT
        		.subtract(INCOME_TAX_FIRST_LIMIT)
                .multiply(INCOME_TAX_SECOND_RATE)
        );

        BigDecimal thirdSection = totalEarnings.subtract(INCOME_TAX_SECOND_LIMIT);

        return tax.add(thirdSection.multiply(INCOME_TAX_THIRD_RATE));
    }

    private BigDecimal calculateLocalIncomeTax(BigDecimal incomeTax) {
        return calculateByRate(incomeTax, LOCAL_INCOME_TAX_RATE);
    }

    private BigDecimal calculateByRate(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate);
    }
}
