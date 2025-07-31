package  com.example.expensetracker.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.expensetracker.dto.ExpenseDTO;
import com.example.expensetracker.entity.ProfileEntity;
import com.example.expensetracker.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;

    @Value("${expensetracker.frontend.url}")
    private String frontendUrl;
    

   // AT 10 pm 
   @Scheduled(cron="0 0 22 * * *",zone="IST")
    public void sendDailyIncomeExpenseReminder()
    {
        log.info("Job started to send daily income and expense reminder emails");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for(ProfileEntity profile :profiles)
        {
            String body = "Hi"+ profile.getFullName() + ",\n\n" +
                    "This is a reminder to add your income and expenses for today.\n" +
                    "<a href=" +frontendUrl + " style='display:inline-block;padding:10px 20px; font-weight:bold;border-radius:5px;text-decoration:none;color:#fff'>Go to expensetracker</a>" +
                    "Thank you!";

        emailService.sendEmail(profile.getEmail(),"Daily reminder:Add your income and expenses " , body);
        }
        log.info("Job completed to send daily income and expense reminder emails");
        
    }


    @Scheduled(cron = "0 0 23 * * *", zone = "IST")

    public void sendDailyExpenseSummary()
    {
        log.info("job statretd :sendDailyExpenseSummary");
        List<ProfileEntity> profiles = profileRepository.findAll();

        for(ProfileEntity profile : profiles)
        {
            List<ExpenseDTO> todaysExpenses = expenseService.getExpensesForUserOnDate(profile.getId(), LocalDate.now());
            
            if(!todaysExpenses.isEmpty())
            {
                StringBuilder table = new StringBuilder();
                table.append("<table style='border-collapse:collapse;width:100%;'>");
                table.append("<tr style='background-color:#f2f2f2'><th style='border:1px solid #ddd;padding:8px;'>Amount</th><th style='style='border:1px solid #ddd;padding:8px;'>category</th><th style='border:1px solid #ddd;padding:8px;'>Category</th></tr>");

                int i=1;
                for(ExpenseDTO expense :todaysExpenses)
                {
                    table.append("<tr>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(i++).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getName()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getAmount()).append("</td>");
                    table.append("<td style='border:1px solid #ddd;padding:8px;'>").append(expense.getCategoryId() !=null ? expense.getCategoryName():"N/A").append("</td>");
                    table.append("</td>");


                }
                table.append("</table>");
                String body = "Hi"+profile.getFullName()+",<br/><br/> here is a summary of your expenses for today:<br/><br/>"+table+"<br/><br/>best regards";
                emailService.sendEmail(profile.getEmail(),"Your daily expense summary",body);

            }
        }
        log.info("job completed :sendDailyExpenseSummary");

    }
    
}
