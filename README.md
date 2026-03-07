# MotorPH Basic Payroll System

Repository: MO-IT101-Group40

---

## Team Details

| Member | Contribution |
|---|---|
| Donalynne May Soriano | Part 1 – Login System |
| Donalynne May Soriano | Part 2 – Menu System |
| Donalynne May Soriano | Part 3 – CSV Reading |
| Jenella Rose Mendoza | Part 4 – Date Filtering & Cutoff Logic |
| Donatella Salanga | Part 5 – Hours Worked Computation |
| Arjun Manzano | Part 6 – Payroll Processing |
| Dhanielle Marie Francisco | Part 7 – Government Deductions |

---

## Program Details

MotorPH Basic Payroll System is a Java console application that processes employee payroll using attendance records from a CSV file. The system allows employees to log in and view their information, while payroll staff can process payroll for one employee or all employees. The system calculates payroll based on cutoff periods and applies government deductions based on the employee’s monthly gross salary, which are applied only to the second cutoff.

---

## How to Run the Program

### 1. Open the Project
Open the project using NetBeans.

### 2. Prepare the CSV File
Make sure the file `motorph_payroll_data.csv` is inside the project folder.

### 3. Run the Program
Run the file `MotorPHPayrollSystem.java`.

---

## Test Employee Login

Enter:

Username: employee  
Password: 12345  

Menu options:

1. Enter your employee number  
2. Exit the program  

Example test:

1  
10001  

The program will display:
- Employee Number
- Employee Name
- Birthday
- Attendance records from June to December

---

## Test Payroll Staff Login

Enter:

Username: payroll_staff  
Password: 12345  

Menu options:

1. Process Payroll  
2. Exit the program  

Sub-menu:

1. One employee  
2. All employees  
3. Exit the program  

### One Employee Payroll Example

1  
10001  

The program will display:
- Cutoff 1 hours worked
- Cutoff 1 gross salary
- Cutoff 1 net salary
- Cutoff 2 hours worked
- Cutoff 2 gross salary
- SSS deduction
- PhilHealth deduction
- Pag-IBIG deduction
- Tax deduction
- Total deductions
- Net salary

### All Employees Payroll Example

2  

The program will display payroll details for all employees in the CSV file.

---

## Project Plan Link

[MotorPH Project Plan Group 40](https://drive.google.com/drive/folders/1NkuK9ec7Do3QY5mTy2zN_OlPYIm_n15H?usp=sharing)
