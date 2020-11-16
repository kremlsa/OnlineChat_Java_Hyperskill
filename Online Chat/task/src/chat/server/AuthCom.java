package chat.server;

public class AuthCom implements Command {
    private ServerBack server;

    public AuthCom(ServerBack server) {
        this.server = server;
    }

    @Override
    public void execute() {

    }
}
