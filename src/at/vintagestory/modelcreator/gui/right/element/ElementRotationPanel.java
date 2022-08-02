package at.vintagestory.modelcreator.gui.right.element;

import at.vintagestory.modelcreator.gui.RotationPanel;
import at.vintagestory.modelcreator.interfaces.IElementManager;
import at.vintagestory.modelcreator.model.Element;

public class ElementRotationPanel extends RotationPanel {
    private final IElementManager manager;

    public ElementRotationPanel(IElementManager manager) {
        this.manager = manager;
    }

    @Override
    protected double[] getElementRotation() {
        Element cube = manager.getCurrentElement();
        if (cube == null) return null;

        return new double[]{ cube.getRotationX(), cube.getRotationY(), cube.getRotationZ() };
    }

    @Override
    protected void setElementRotation(double x, double y, double z) {
        Element cube = manager.getCurrentElement();
        if (cube == null) return;

        cube.setRotationX(x);
        cube.setRotationY(y);
        cube.setRotationZ(z);
    }
}
