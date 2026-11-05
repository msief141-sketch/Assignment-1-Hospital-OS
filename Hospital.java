import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Hospital {
    private static final Object OUTPUT_LOCK = new Object();

    public static void log(String message) {
        synchronized (OUTPUT_LOCK) {
            System.out.println(message);
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int capacity = readPositiveInt(scanner, "Waiting room capacity: ");
            int numberOfDoctors = readPositiveInt(scanner, "Number of doctors: ");
            int numberOfPatients = readPositiveInt(scanner, "Number of patients: ");

            WaitingRoom waitingRoom = new WaitingRoom(capacity);
            TreatmentRoom treatmentRoom = new TreatmentRoom();
            CountDownLatch allPatientsTreated = new CountDownLatch(numberOfPatients);

            List<Doctor> doctors = new ArrayList<>();
            for (int i = 1; i <= numberOfDoctors; i++) {
                doctors.add(new Doctor(i, waitingRoom, treatmentRoom, allPatientsTreated));
            }

            List<Patient> patients = new ArrayList<>();
            for (int i = 1; i <= numberOfPatients; i++) {
                patients.add(new Patient(i, waitingRoom));
            }

            System.out.println("\nPatients arriving:");
            for (int i = 1; i <= numberOfPatients; i++) {
                System.out.print("P" + i + (i == numberOfPatients ? "\n" : " "));
            }

            // Start consumers before producers so doctors are ready for arriving patients.
            for (Doctor doctor : doctors) {
                doctor.start();
            }

            for (Patient patient : patients) {
                patient.start();
                // Small random arrival gap makes the concurrent events easier to follow.
                Thread.sleep((long) (Math.random() * 400));
            }

            // Ensure every patient thread has finished attempting to enter the buffer.
            for (Patient patient : patients) {
                patient.join();
            }

            // Wait until every patient has completed treatment and left the hospital.
            allPatientsTreated.await();

            // No patients remain in the buffer; wake doctors so they can terminate.
            waitingRoom.close(numberOfDoctors);

            for (Doctor doctor : doctors) {
                doctor.join();
            }

            log("All patients have been treated.");
            log("Simulation completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("The hospital simulation was interrupted.");
        } catch (IllegalArgumentException e) {
            System.out.println("Input error: " + e.getMessage());
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                if (value > 0) {
                    return value;
                }
            } else {
                scanner.next();
            }
            System.out.println("Please enter a positive integer.");
        }
    }
}