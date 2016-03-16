package co.favorie.at.datum;

/**
 * Created by bmac on 2015-08-03.
 */
public class ATDatumForTransmit {
    private static ATDatumForTransmit ourInstance = new ATDatumForTransmit();
    public ATScheduleDatum atScheduleDatum;
    public boolean completed;

    public static ATDatumForTransmit getInstance() {
        if(ourInstance.atScheduleDatum == null) {
            ourInstance.atScheduleDatum = new ATScheduleDatum();
            ourInstance.completed = false;
        }
        return ourInstance;
    }

    public ATDatumForTransmit() {
    }
}
