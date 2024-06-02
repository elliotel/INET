/*
 * För att kunna dela variabeln state mellan Server och instanser av ServerThread
 * så delar vi ett mutable object "StateHolder" som håller strängen state
 * 
 */
public class StateHolder {
    private String state;

    public StateHolder(String initialState) {
        state = initialState;
    }

    public synchronized String getState() {
        return state;
    }

    public synchronized void setState(String newState) {
        state = newState;
    }
}