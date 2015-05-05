package DataStructures;

import java.util.List;

/**
 * Created by Administrator on 4/28/2015.
 */
public class Goal {
    public Status Status;
    public Integer HashCoordinates;
    public Character Letter;

    // Goals this goal rely on to get satisfied
    public List<Goal> RelyingGoals;

    // Goals that depend on this goal
    public List<Goal> DependentGoals;

    public Goal(Integer coordinates, char goalLetter, Status status) {
        HashCoordinates = coordinates;
        Letter = goalLetter;
        Status = status;
    }

    @Override
    public int hashCode() {
        return HashCoordinates;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Goal){
            Goal other = (Goal) obj;
            return this.Letter == other.Letter && this.HashCoordinates == other.HashCoordinates;
        }

        return false;
    }
}
