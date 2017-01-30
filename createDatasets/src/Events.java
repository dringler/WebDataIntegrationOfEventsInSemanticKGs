import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 28/01/17.
 */
@XmlRootElement(name = "events")
public class Events {
    private List<Event> events = new ArrayList<>();

    public Events(){};

    @XmlElement(name = "event")
    public List<Event> getEvents() {
        return events;
    }
    public void addEvent(Event event) {
        events.add(event);
    }

}
