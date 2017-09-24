package pullup.ctd.com.myapplication;

/**
 * Created by steph on 9/24/2017.
 */

class PullUpEvents {

    String eventName;
    String eventType;
    String eventAddress;
    String eventImage;
    String eventHost;
    String eventStartTime;
    String eventStatus;

    public PullUpEvents(String eventName, String eventType, String eventAddress, String eventImage, String eventHost, String eventStartTime, String eventStatus) {
        this.eventName = eventName;
        this.eventType = eventType;
        this.eventAddress = eventAddress;
        this.eventImage = eventImage;
        this.eventHost = eventHost;
        this.eventStartTime = eventStartTime;
        this.eventStatus = eventStatus;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventAddress() {
        return eventAddress;
    }

    public void setEventAddress(String eventAddress) {
        this.eventAddress = eventAddress;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getEventHost() {
        return eventHost;
    }

    public void setEventHost(String eventHost) {
        this.eventHost = eventHost;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }


    @Override
    public String toString() {
        return "PullUpEvents{" +
                "eventName='" + eventName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventAddress='" + eventAddress + '\'' +
                ", eventImage='" + eventImage + '\'' +
                ", eventHost='" + eventHost + '\'' +
                ", eventStartTime='" + eventStartTime + '\'' +
                ", eventStatus='" + eventStatus + '\'' +
                '}';
    }
}
