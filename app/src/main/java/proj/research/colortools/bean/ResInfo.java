package proj.research.colortools.bean;

public class  ResInfo <T> {
    private boolean err;
    private int status;
    private T msg;

    public boolean isErr() {
        return err;
    }

    public void setErr(boolean err) {
        this.err = err;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getMsg() {
        return msg;
    }


    public void setMsg(T msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResInfo{" +
                "err=" + err +
                ", status=" + status +
                ", msg=" + msg +
                '}';
    }
}
