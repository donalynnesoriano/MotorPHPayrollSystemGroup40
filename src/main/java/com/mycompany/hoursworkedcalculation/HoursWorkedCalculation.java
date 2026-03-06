
public class HoursWorkedCalculation {
    
    // 8:00 AM in minutes from midnight (8 * 60)
    private static final int START_OF_DAY = 480;
    // 5:00 PM in minutes from midnight (17 * 60)
    private static final int END_OF_DAY = 1020;
    
    /**
     * Converts HH:MM string to total minutes from midnight
     * @param time
     */
    public int toMinutes(String time) {
            String[] parts = time.split(";");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            // Handle PM conversion if not using 24-hour clock
       
            
            return (hours * 60) + minutes;
    }
    /**
     * Computes hours worked based on the 8-5 rule.
     */
    public double computeDailyHours(String entryTime, String exitTime)   {
        int entry = toMinutes(entryTime);
        int exit = toMinutes(exitTime);
        
        // Apply rules: "Ignore extra hours"
        // Clamp entry to 8:00 AM (earliest)
        int actualStart = Math.max(entry, START_OF_DAY);
        // Clamp exit to 5:00 AM (latest)
        int actualEnd = Math.min(exit, END_OF_DAY);
        int BREAK_MINUTES = 0;
        
        // Calculate duration
        int totalMinutes = actualEnd - actualStart - BREAK_MINUTES;
        
        // Ensure we don't return negative hours if they left before break
        return Math.max(0, totalMinutes) / 60.0;
   
        
    }
}
