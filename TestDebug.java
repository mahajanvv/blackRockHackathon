import com.blackrock.retirement.domain.FinancialProjection;
import com.blackrock.retirement.service.FinancialProjectionService;

public class TestDebug {
    public static void main(String[] args) {
        FinancialProjectionService projectionService = new FinancialProjectionService();
        
        FinancialProjection projection = projectionService.calculateNPS(
                100000.0,
                30.0,
                0.0,
                0.0
        );
        
        System.out.println("Principal: " + projection.getPrincipal());
        System.out.println("Rate: " + projection.getRate());
        System.out.println("Time Horizon: " + projection.getTimeHorizon());
        System.out.println("Future Value: " + projection.getFutureValue());
        System.out.println("Real Value: " + projection.getRealValue());
        System.out.println("Tax Benefit: " + projection.getTaxBenefit());
        
        // Manual calculation
        double manualFutureValue = 100000.0 * Math.pow(1.0711, 30.0);
        System.out.println("Manual Future Value Calculation: " + manualFutureValue);
        System.out.println("Is > 800000: " + (projection.getFutureValue() > 800000.0));
    }
}
