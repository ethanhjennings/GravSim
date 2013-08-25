package Panel;

public interface SettingsPanelListener {
	public class SettingsPanelOptions {
		public int calculationAccuracy;
		public int smoothFieldWidth;
		public boolean useBilinearSmoothing;
		public int vectorFieldSpacing;
		public int tailLength;
		public int gridWidth;
		public boolean showingVectorHeads;
	}
	public void dialogClosed(SettingsPanelOptions options, boolean settingsChanged);
}
