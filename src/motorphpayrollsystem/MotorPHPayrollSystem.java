package motorphpayrollsystem;

import java.io.*;
import java.util.*;

public class MotorPHPayrollSystem {

    static final String CSV_FILE = "motorph_payroll_data.csv";

    // =========================================================
    // PART 1 — LOGIN SYSTEM
    // =========================================================
    public static void main(String[] args) {
        login();
    }

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

    // =========================================================
    // PART 2 — MENU SYSTEM
    // =========================================================
    static void employeeMenu(Scanner sc) {
        while (true) {
            System.out.println("\nIf username is: employee");
            System.out.println("Display options:");
            System.out.println("1. Enter your employee number");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("2")) {
                System.out.println("Terminate the program.");
                return;
            }

            if (!choice.equals("1")) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            System.out.print("Enter your employee number: ");
            String empNo = sc.nextLine().trim();

            List<String[]> rows = readCSVRows();
            Map<String, List<String[]>> byEmp = groupByEmployee(rows);

            if (!byEmp.containsKey(empNo)) {
                System.out.println("Employee number does not exist.");
                continue;
            }

            String[] first = byEmp.get(empNo).get(0);

            System.out.println("Employee Number: " + first[0]);
            System.out.println("Employee Name: " + first[1]);
            System.out.println("Birthday: " + first[2]);
        }
    }

    static void payrollStaffMenu(Scanner sc) {
        while (true) {
            System.out.println("\nIf username is: payroll_staff");
            System.out.println("Display options:");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit the program");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("2")) {
                System.out.println("Terminate the program.");
                return;
            }

            if (!choice.equals("1")) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }

            processPayrollMenu(sc);
        }
    }

    static void processPayrollMenu(Scanner sc) {
        while (true) {
            System.out.println("\nProcess Payroll (Do not include allowances)");
            System.out.println("Display sub-options:");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit the program");
            System.out.print("Choose: ");

            String choice = sc.nextLine().trim();

            List<String[]> rows = readCSVRows();
            Map<String, List<String[]>> byEmp = groupByEmployee(rows);

            if (choice.equals("1")) {
                System.out.print("Enter the employee number: ");
                String empNo = sc.nextLine().trim();

                if (!byEmp.containsKey(empNo)) {
                    System.out.println("Employee number does not exist.");
                    continue;
                }

                processPayrollForEmployee(empNo, byEmp.get(empNo));

            } else if (choice.equals("2")) {
                for (String empNo : byEmp.keySet()) {
                    processPayrollForEmployee(empNo, byEmp.get(empNo));
                    System.out.println();
                }

            } else if (choice.equals("3")) {
                System.out.println("Terminate the program.");
                return;

            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }

    // =========================================================
    // PART 3 — CSV READING
    // =========================================================
    static List<String[]> readCSVRows() {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line = br.readLine(); // skip header
            if (line == null) return rows;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length < 7) continue;

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }

                rows.add(data);
            }

        } catch (FileNotFoundException e) {
            System.out.println("CSV not found: " + CSV_FILE);
            System.out.println("Put it in the project folder (same level as src).");
        } catch (Exception e) {
            System.out.println("Error reading CSV.");
            e.printStackTrace();
        }

        return rows;
    }

    static Map<String, List<String[]>> groupByEmployee(List<String[]> rows) {
        Map<String, List<String[]>> map = new TreeMap<>();

        for (String[] row : rows) {
            String empNo = row[0];
            map.putIfAbsent(empNo, new ArrayList<>());
            map.get(empNo).add(row);
        }

        return map;
    }

    // =========================================================
    // PART 4 — DATE FILTERING & CUTOFF LOGIC
    // =========================================================
    static boolean isJuneToDecember(String date) {
        if (date == null || date.length() < 7) return false;

        String[] parts = date.split("-");
        if (parts.length < 3) return false;

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
        for (String[] row : empRows) {
            String date = row[4];
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

    static String getMonthName(int month) {
        String[] monthNames = {
            "", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        return monthNames[month];
    }

    static int getLastDayOfMonth(int month) {
        if (month == 9 || month == 4 || month == 6 || month == 11) return 30;
        if (month == 2) return 28;
        return 31;
    }

    // =========================================================
    // PART 5 — HOURS WORKED COMPUTATION
    // =========================================================
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

            // Special rule from instructions:
            // 8:05 AM to 5:00 PM = 8 hours
            if (startMinutes >= 480 && startMinutes <= 485 && endMinutes == 1020) {
                return 8.0;
            }

            int totalMinutes = endMinutes - startMinutes;

            // 1-hour break deduction to match examples like 8:30 to 5:30 = 7.5 hrs
            totalMinutes -= 60;

            if (totalMinutes < 0) totalMinutes = 0;

            return totalMinutes / 60.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    // =========================================================
    // PART 6 — PAYROLL PROCESSING
    // =========================================================
    static void processPayrollForEmployee(String empNo, List<String[]> empRows) {
        String[] first = empRows.get(0);
        String name = first[1];
        String birthday = first[2];
        double hourlyRate = parseDoubleSafe(first[3]);

        Map<String, Double> cutoffHours = new TreeMap<>();

        for (String[] row : empRows) {
            String date = row[4];
            if (!isJuneToDecember(date)) continue;

            String timeIn = row[5];
            String timeOut = row[6];

            double hours = computeDailyHours(timeIn, timeOut);
            String key = cutoffKey(date);

            cutoffHours.put(key, cutoffHours.getOrDefault(key, 0.0) + hours);
        }

        System.out.println("Employee #: " + empNo);
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

            double sss = computeSSS(monthlyGross);
            double philHealth = computePhilHealth(monthlyGross);
            double pagIbig = computePagIbig(monthlyGross);
            double tax = computeTax(monthlyGross);

            double totalDeductions = sss + philHealth + pagIbig + tax;

            double net1 = gross1;
            double net2 = gross2 - totalDeductions;

            int lastDay = getLastDayOfMonth(month);
            String monthName = getMonthName(month);

            System.out.println("Cutoff Date: " + monthName + " 1 to " + monthName + " 15");
            System.out.println("Total Hours Worked: " + h1);
            System.out.println("Gross Salary: " + gross1);
            System.out.println("Net Salary: " + net1);

            System.out.println("Cutoff Date: " + monthName + " 16 to " + monthName + " " + lastDay + " (Second payout includes all deductions)");
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

    // =========================================================
    // PART 7 — GOVERNMENT DEDUCTIONS
    // =========================================================
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

    // =========================================================
    // EXTRA HELPER
    // =========================================================
    static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}

    