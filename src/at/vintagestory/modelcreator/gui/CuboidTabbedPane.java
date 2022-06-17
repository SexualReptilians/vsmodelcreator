package at.vintagestory.modelcreator.gui;

import java.awt.*;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.interfaces.IValueUpdater;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

public class CuboidTabbedPane extends JTabbedPane
{
	private static final long serialVersionUID = 1L;

	protected IElementManager manager;

	public CuboidTabbedPane(IElementManager manager)
	{
		this.manager = manager;
		this.setUI(new SaneTabbedPaneUI());
	}

	public void updateValues(JComponent byGuiElem)
	{
		for (int i = 0; i < getTabCount(); i++)
		{
			Component component = getComponentAt(i);
			if (component != null)
			{
				if (component instanceof IValueUpdater)
				{
					IValueUpdater updater = (IValueUpdater) component;
					updater.updateValues(byGuiElem);
				}
			}
		}
	}

	@Override
	public String getUIClassID() {
		return "SaneTabbedPaneUI";
	}

	@Override
	public void updateUI() {

	}

	private class SaneTabbedPaneUI extends FlatTabbedPaneUI {

		@Override
		protected LayoutManager createLayoutManager() {
			return new BasicTabbedPaneUI.TabbedPaneLayout() {

				@Override
				protected void rotateTabRuns(
						int tabPlacement, int selectedRun) {}

				@Override
				protected void padSelectedTab(
						int tabPlacement, int selectedIndex) {}
			};
		}
	}
}
