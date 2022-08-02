package at.vintagestory.modelcreator.gui.right.attachmentpoints;

import at.vintagestory.modelcreator.ModelCreator;
import at.vintagestory.modelcreator.gui.RotationPanel;
import at.vintagestory.modelcreator.model.AttachmentPoint;

public class AttachmentPointRotPanel extends RotationPanel {
	@Override
	protected double[] getElementRotation() {
		AttachmentPoint point = ModelCreator.currentProject.SelectedAttachmentPoint;
		if (point == null) return null;

		return new double[]{ point.getRotationX(), point.getRotationY(), point.getRotationZ() };
	}

	@Override
	protected void setElementRotation(double x, double y, double z) {
		AttachmentPoint point = ModelCreator.currentProject.SelectedAttachmentPoint;
		if (point == null) return;

		point.setRotationX(x);
		point.setRotationY(y);
		point.setRotationZ(z);
	}
}
