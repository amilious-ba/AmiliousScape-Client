package rt4.amilious.Commands;

public abstract class AbstractCommand implements ICommand {

    private final String command;
    private final boolean startsWith;
    private final boolean ignoreCase;

    public AbstractCommand(String command, boolean startsWith, boolean ignoreCase) {
        this.command = command;
        this.startsWith = startsWith;
        this.ignoreCase = ignoreCase;
    }

    public AbstractCommand(String command, boolean startsWith) {
        this(command,startsWith,false);
    }

    public AbstractCommand(String command) {
        this(command,true);
    }

    @Override
    public boolean compare(String s) {
        if(startsWith) {
            if(ignoreCase) return s.toLowerCase().startsWith(command.toLowerCase());
            else return s.startsWith(command);
        }
        if(ignoreCase) return s.equalsIgnoreCase(command);
        else return s.equals(command);
    }

}
