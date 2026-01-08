package com.example.appointment.Appointment;

import com.example.appointment.Common.enums.NotificationType;
import com.example.appointment.Notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AppointmentSchedulerService {

    private final AppointmentService appointmentService;
    private final NotificationService notificationService;

    public AppointmentSchedulerService(@org.springframework.beans.factory.annotation.Autowired @org.springframework.context.annotation.Lazy AppointmentService appointmentService,
                                      NotificationService notificationService) {
        this.appointmentService = appointmentService;
        this.notificationService = notificationService;
    }
    
    // In-memory storage for scheduled tasks (in a production environment, you might want to use a database)
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    // Thread pool for scheduling tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    /**
     * Schedule a job to run when the appointment time arrives
     * @param appointment The appointment to schedule
     */
    public void scheduleAppointmentJob(Appointment appointment) {
        if (appointment.getFrom() == null) {
            log.warn("Cannot schedule appointment job: appointment {} has no start time", appointment.getId());
            return;
        }

        // Cancel any existing scheduled task for this appointment
        cancelScheduledJob(appointment.getId());

        // Calculate delay until appointment time
        long delay = calculateDelay(appointment.getFrom());
        
        if (delay <= 0) {
            // Appointment time has already passed, run immediately
            log.warn("Appointment {} time has already passed, running immediately", appointment.getId());
            runAppointmentJob(appointment.getId());
            return;
        }

        // Schedule the task
        ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> runAppointmentJob(appointment.getId()), delay, TimeUnit.MILLISECONDS);
        scheduledTasks.put(appointment.getId(), scheduledTask);
        
        log.info("Scheduled appointment job for appointment ID: {} at time: {}, delay: {} ms", 
                 appointment.getId(), appointment.getFrom(), delay);
    }

    /**
     * Cancel a scheduled job for an appointment
     * @param appointmentId The ID of the appointment to cancel
     */
    public void cancelScheduledJob(Long appointmentId) {
        ScheduledFuture<?> existingTask = scheduledTasks.get(appointmentId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false); // false means don't interrupt if running
            scheduledTasks.remove(appointmentId);
            log.info("Cancelled scheduled job for appointment ID: {}", appointmentId);
        }
    }

    /**
     * Calculate the delay in milliseconds until the appointment time
     * @param appointmentTime The time of the appointment
     * @return Delay in milliseconds
     */
    private long calculateDelay(LocalDateTime appointmentTime) {
        LocalDateTime now = LocalDateTime.now();
        if (appointmentTime.isBefore(now)) {
            return 0; // Time has already passed
        }
        
        long delaySeconds = java.time.Duration.between(now, appointmentTime).getSeconds();
        return delaySeconds * 1000; // Convert to milliseconds
    }

    /**
     * The actual job that runs when the appointment time arrives
     * @param appointmentId The ID of the appointment to process
     */
    @Async
    public void runAppointmentJob(Long appointmentId) {
        try {
            // Fetch the appointment from the database using the repository directly to avoid circular dependency
            Optional<Appointment> appointmentOpt = appointmentService.getAppointmentRepository().findById(appointmentId);

            if (appointmentOpt.isEmpty()) {
                log.error("Appointment with ID {} not found", appointmentId);
                return;
            }

            Appointment appointment = appointmentOpt.get();

            // Check if the appointment is still scheduled (not cancelled)
            if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
                log.info("Appointment {} is no longer scheduled (status: {}), skipping job",
                         appointmentId, appointment.getStatus());
                return;
            }

            // Update appointment status to COMPLETED
            appointmentService.updateAppointmentStatus(appointmentId, Appointment.AppointmentStatus.COMPLETED);

            // Send notification to the customer
            String message = String.format("Your appointment scheduled for %s has been completed.",
                                          appointment.getFrom().toString());
            notificationService.createNotification(appointment.getCustomer(), NotificationType.EXECUTED, message);

            log.info("Appointment {} completed successfully and customer notified", appointmentId);

        } catch (Exception e) {
            log.error("Error running appointment job for appointment ID: {}", appointmentId, e);
        }
    }

    /**
     * Periodically clean up completed or cancelled tasks from the scheduledTasks map
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void cleanupCompletedTasks() {
        scheduledTasks.entrySet().removeIf(entry -> {
            ScheduledFuture<?> task = entry.getValue();
            if (task.isDone()) {
                log.debug("Removing completed task for appointment ID: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}