package at.vintagestory.modelcreator.gui.right.keyframes;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.gui.RotationPanel;
import at.vintagestory.modelcreator.model.KeyFrameElement;
import at.vintagestory.modelcreator.util.QUtil;

import javax.swing.*;

public class ElementKeyFrameRotationPanel extends RotationPanel {
	private final RightKeyFramesPanel keyFramesPanel;

	private boolean ignoreSliderChanges;
	public boolean enabled;

	public ElementKeyFrameRotationPanel(RightKeyFramesPanel keyFramesPanel) {
		this.keyFramesPanel = keyFramesPanel;
	}

	@Override
	protected double[] getElementRotation() {
		KeyFrameElement elem = keyFramesPanel.getCurrentElement();
		if (elem == null) return null;

		return new double[]{ elem.getRotationX(), elem.getRotationY(), elem.getRotationZ() };
	}

	@Override
	protected void setElementRotation(double x, double y, double z) {
		if (ignoreSliderChanges || !enabled) return;

		SwingUtilities.invokeLater(() -> {
			KeyFrameElement elem = keyFramesPanel.getCurrentElement();
			if (elem == null) return;

			elem.setRotationX(x);
			elem.setRotationY(y);
			elem.setRotationZ(z);
			ModelCreator.updateValues(this);
		});
	}

	public void toggleFields(KeyFrameElement element, JComponent byGuiElem) {
		this.ignoreSliderChanges = element == null;

		if (element != null) {
			quaternion.set(QUtil.ToQuaternion(
					Math.toRadians(element.getRotationZ()), // z
					Math.toRadians(element.getRotationY()), // y
					Math.toRadians(element.getRotationX())  // x
			));
		}
		ModelCreator.ignoreValueUpdates = true;
		updateValues(byGuiElem);
		ModelCreator.ignoreValueUpdates = false;
	}
}
