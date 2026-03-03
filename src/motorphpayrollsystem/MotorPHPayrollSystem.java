package motorphpayrollsystem;

import java.io.*;
import java.util.*;

public class MotorPHPayrollSystem {

   
    static final String CSV_FILE = "motorph_payroll_data.csv";

 
    public static void main(String[] args) {
        login();
    }

    // ====== LOGIN ======
    static void login() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        boolean validUser = username.equals("employee") || username.equals("payroll_staff");
        boolean validPass = password.equals("12345");

        if (!validUser || !validPass) {
            System.out.println("Incorrect username and/or password.");
            System.out.println("Program terminated.");
            return;
        }

        if (username.equals("employee")) {
            employeeMenu(sc);
        } else {
            payrollStaffMenu(sc);
        }
    }

    // ====== EMPLOYEE MENU ======
    static void employeeMenu(Scanner sc) {
        while (true) {
            System.out.println("\n=== EMPLOYEE MENU ===");
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("2")) {
                System.out.println("Program terminated.");
                return;
            }

            if (!choice.equals("1")) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            System.out.print("Enter Employee Number: ");
            String empNo = sc.nextLine().trim();

            List<String[]> rows = readCSVRows();
            Map<String, List<String[]>> byEmp = groupByEmployee(rows);

            if (!byEmp.containsKey(empNo)) {
                System.out.println("Employee number does not exist.");
                continue;
            }

            // Display employee details + June–Dec attendance records (with computed hours)
            String[] first = byEmp.get(empNo).get(0);
            String name = first[1];
            String birthday = first[2];

            System.out.println("\nEmployee Number: " + empNo);
            System.out.println("Employee Name: " + name);
            System.out.println("Birthday: " + birthday);

            System.out.println("\nJune to December Attendance Records:");
            System.out.println("Date | TimeIn-TimeOut | Hours Worked");
            for (String[] r : byEmp.get(empNo)) {
                String date = r[4];
                if (!isJuneToDecember(date)) continue;

                String timeIn = r[5];
                String timeOut = r[6];
                double hours = computeDailyHours(timeIn, timeOut);
                System.out.println(date + " | " + timeIn + "-" + timeOut + " | " + hours);
            }
        }
    }

    // ====== PAYROLL STAFF MENU ======
    static void payrollStaffMenu(Scanner sc) {
        while (true) {
            System.out.println("\n=== PAYROLL STAFF MENU ===");
            System.out.println("1. Process Payroll - One Employee");
            System.out.println("2. Process Payroll - All Employees");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("3")) {
                System.out.println("Goodbye!");
                return;
            }

            List<String[]> rows = readCSVRows();
            Map<String, List<String[]>> byEmp = groupByEmployee(rows);

            if (choice.equals("1")) {
                System.out.print("Enter Employee Number: ");
                String empNo = sc.nextLine().trim();

                if (!byEmp.containsKey(empNo)) {
                    System.out.println("Employee number does not exist.");
                    continue;
                }

                processPayrollForEmployee(empNo, byEmp.get(empNo));

            } else if (choice.equals("2")) {
                for (String empNo : byEmp.keySet()) {
                    processPayrollForEmployee(empNo, byEmp.get(empNo));
                    System.out.println("--------------------------------------------------");
                }

            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // ====== PROCESS PAYROLL (ONE EMPLOYEE) ======
    static void processPayrollForEmployee(String empNo, List<String[]> empRows) {
        // get employee details from first row
        String[] first = empRows.get(0);
        String name = first[1];
        String birthday = first[2];
        double hourlyRate = parseDoubleSafe(first[3]);

        // Build cutoff totals: Key like "2025-06|C1" or "2025-06|C2"
        Map<String, Double> cutoffHours = new TreeMap<>();
        for (String[] r : empRows) {
            String date = r[4];
            if (!isJuneToDecember(date)) continue;

            String timeIn = r[5];
            String timeOut = r[6];

            double hours = computeDailyHours(timeIn, timeOut);
            String cutoffKey = cutoffKey(date); // YYYY-MM|C1 or YYYY-MM|C2

            cutoffHours.put(cutoffKey, cutoffHours.getOrDefault(cutoffKey, 0.0) + hours);
        }

      
        System.out.println("\nEmployee #: " + empNo);
        System.out.println("Employee Name: " + name);
        System.out.println("Birthday: " + birthday);

        for (int month = 6; month <= 12; month++) {
            String ym = yearMonthFromAnyRecord(empRows, month);
            
            String keyC1 = ym + "|C1";
            String keyC2 = ym + "|C2";

            double h1 = cutoffHours.getOrDefault(keyC1, 0.0);
            double h2 = cutoffHours.getOrDefault(keyC2, 0.0);

            double gross1 = h1 * hourlyRate;
            double gross2 = h2 * hourlyRate;

            double monthlyGross = gross1 + gross2;

            // Government deductions rule: add cutoff1 + cutoff2 first, THEN compute deductions
            double sss = computeSSS(monthlyGross);
            double philHealth = computePhilHealth(monthlyGross);
            double pagIbig = computePagIbig(monthlyGross);
            double tax = computeTax(monthlyGross);

            double totalDeductions = sss + philHealth + pagIbig + tax;

            // As per note: "Second payout includes all deductions"
            double net1 = gross1;                 // no deductions on 1st cutoff
            double net2 = gross2 - totalDeductions;

            // --- Print ---
            System.out.println("\nCutoff Date: " + ym + " (June–Dec) - 1st cutoff (1 to 15)");
            System.out.println("Total Hours Worked: " + h1);
            System.out.println("Gross Salary: " + gross1);
            System.out.println("Net Salary: " + net1);

            System.out.println("\nCutoff Date: " + ym + " - 2nd cutoff (16 to end) (Second payout includes all deductions)");
            System.out.println("Total Hours Worked: " + h2);
            System.out.println("Gross Salary: " + gross2);
            System.out.println("Each Deduction:");
            System.out.println("SSS: " + sss);
            System.out.println("PhilHealth: " + philHealth);
            System.out.println("Pag-IBIG: " + pagIbig);
            System.out.println("Tax: " + tax);
            System.out.println("Total Deductions: " + totalDeductions);
            System.out.println("Net Salary: " + net2);
        }
    }

    // ====== CSV READING ======
    static List<String[]> readCSVRows() {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line = br.readLine(); // header
            if (line == null) return rows;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
   
                if (data.length < 7) continue;

                for (int i = 0; i < data.length; i++) data[i] = data[i].trim();
                rows.add(data);
            }

        } catch (FileNotFoundException e) {
            System.out.println("CSV not found: " + CSV_FILE);
            System.out.println("Put it in the project folder (same level as src).");
            System.out.println("Current folder is: " + System.getProperty("user.dir"));
        } catch (Exception e) {
            System.out.println("Error reading CSV.");
            e.printStackTrace();
        }

        return rows;
    }

    static Map<String, List<String[]>> groupByEmployee(List<String[]> rows) {
        Map<String, List<String[]>> map = new TreeMap<>();
        for (String[] r : rows) {
            String empNo = r[0];
            map.putIfAbsent(empNo, new ArrayList<>());
            map.get(empNo).add(r);
        }
        return map;
    }
   // ====== DATE FILTERING & CUTOFF LOGIC ======

static boolean isJuneToDecember(String date) {
    if (date == null || date.length() < 7) return false;

    String[] parts = date.split("-");
    if (parts.length < 2) return false;

    int month = Integer.parseInt(parts[1]);
    return month >= 6 && month <= 12;
}

static String cutoffKey(String date) {
    String[] parts = date.split("-");
    String year = parts[0];
    String month = parts[1];
    int day = Integer.parseInt(parts[2]);

    String cutoff = (day <= 15) ? "C1" : "C2";

    return year + "-" + month + "|" + cutoff;
}

static String yearMonthFromAnyRecord(List<String[]> empRows, int targetMonth) {
    for (String[] r : empRows) {
        String date = r[4];
        String[] parts = date.split("-");
        int month = Integer.parseInt(parts[1]);

        if (month == targetMonth) {
            return parts[0] + "-" + parts[1];
        }
    }

    String firstDate = empRows.get(0)[4];
    String[] parts = firstDate.split("-");
    return parts[0] + "-" + String.format("%02d", targetMonth);
}

// ====== ATTENDANCE HOURS COMPUTATION ======

static double computeDailyHours(String timeIn, String timeOut) {
    try {
        String[] inParts = timeIn.split(":");
        String[] outParts = timeOut.split(":");

        int inHour = Integer.parseInt(inParts[0]);
        int inMin = Integer.parseInt(inParts[1]);

        int outHour = Integer.parseInt(outParts[0]);
        int outMin = Integer.parseInt(outParts[1]);

        int startMinutes = inHour * 60 + inMin;
        int endMinutes = outHour * 60 + outMin;

        int workStart = 8 * 60;   // 8:00 AM
        int workEnd = 17 * 60;    // 5:00 PM

        if (startMinutes < workStart) startMinutes = workStart;
        if (endMinutes > workEnd) endMinutes = workEnd;

        if (endMinutes <= startMinutes) return 0.0;

        int totalMinutes = endMinutes - startMinutes;

        return totalMinutes / 60.0;

    } catch (Exception e) {
        return 0.0;
    }
}

// ====== SAFE PARSE ======

static double parseDoubleSafe(String value) {
    try {
        return Double.parseDouble(value);
    } catch (Exception e) {
        return 0.0;
    }
}

// ====== DEDUCTION PLACEHOLDERS ======

static double computeSSS(double gross) {
    return gross * 0.05;
}

static double computePhilHealth(double gross) {
    return gross * 0.03;
}

static double computePagIbig(double gross) {
    return gross * 0.02;
}

static double computeTax(double gross) {
    return gross * 0.10;
}

}


    