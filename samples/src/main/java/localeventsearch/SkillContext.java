package localeventsearch;

public class SkillContext {
    private boolean needsMoreHelp = true;
    private boolean saveResultForLater = true;

    public boolean needsMoreHelp() {
        return needsMoreHelp;
    }

    public void setNeedsMoreHelp(boolean needsMoreHelp) {
        this.needsMoreHelp = needsMoreHelp;
    }

    public boolean saveForLater(boolean saveResultForLater) {
    	return this.saveResultForLater;
    }

    public void setSaveForLater(boolean saveResultForLater) {
    	this.saveResultForLater = saveResultForLater;
    }

}
