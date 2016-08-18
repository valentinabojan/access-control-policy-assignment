package api;

import java.io.Serializable;

public class Response implements Serializable {
    private ResponseType type;
    private Object value;

    public Response(ResponseType type) {
        this.type = type;
    }

    public Response(ResponseType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Response response = (Response) o;

        if (type != response.type) return false;
        if (value != null ? !value.equals(response.value) : response.value != null) return false;

        return true;
    }
}
