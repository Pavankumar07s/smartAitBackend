//package net.engineeringdigest.journalApp.sheduler;
//
//import net.engineeringdigest.journalApp.Repo.userRepoIMP;
//import net.engineeringdigest.journalApp.Service.EmailService;
//import net.engineeringdigest.journalApp.entity.JournalEntry;
//import net.engineeringdigest.journalApp.entity.User;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//@Service
//public class UserScheduler {
//    @Autowired
//    private EmailService emailService;
//
//    @Autowired
//    private userRepoIMP userRepository;
//    @Scheduled(cron = "0 0 9 * * SUN")
//    public void fetchUsersAndSendSaMail() {
//        List<User> users = userRepository.getUserForSA();
//        for (User user : users) {
//            List<JournalEntry> journalEntries = user.getJournalEntries();
////            emailService.sendEmail(user.getEmail(),"hehe header hai","yaha hai body");
//            emailService.sendEmailUsingSendGRid(user.getEmail(),"hehe header hai","yaha hai body");
//        }
//    }
//}
