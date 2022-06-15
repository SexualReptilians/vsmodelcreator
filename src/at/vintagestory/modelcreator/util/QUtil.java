package at.vintagestory.modelcreator.util;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

public class QUtil
{

    public static final double EPSILON = 0.000001;

    /**
     * Converts yaw, pitch, roll to a quaternion
     */
	public static Quaternion ToQuaternion(double yaw, double pitch, double roll) // yaw (Z), pitch (Y), roll (X)
	{
	    // Abbreviations for the various angular functions
	    double cy = Math.cos(yaw * 0.5);
	    double sy = Math.sin(yaw * 0.5);
	    double cp = Math.cos(pitch * 0.5);
	    double sp = Math.sin(pitch * 0.5);
	    double cr = Math.cos(roll * 0.5);
	    double sr = Math.sin(roll * 0.5);

	    double w = cy * cp * cr + sy * sp * sr;
	    double x = cy * cp * sr - sy * sp * cr;
	    double y = sy * cp * sr + cy * sp * cr;
	    double z = sy * cp * cr - cy * sp * sr;

	    Quaternion q = new Quaternion((float)x, (float)y, (float)z, (float)w);
	    q.normalise();
	    
	    return q;
	}

    /**
     * Converts intrinsic x/y/z euler angles as used by the game to a hamilton quaternion.
     */
	public static Quaternion IntrinsicXYZToQuaternion(double alpha, double beta, double gamma)
    {
        double ca = Math.cos(alpha * 0.5);
        double sa = Math.sin(alpha * 0.5);
        double cb = Math.cos(beta * 0.5);
        double sb = Math.sin(beta * 0.5);
        double cy = Math.cos(gamma * 0.5);
        double sy = Math.sin(gamma * 0.5);

        double w = ca * cb * cy + sa * sb * sy;
        double x = sa * cb * cy - ca * sb * sy;
        double y = ca * sb * cy + sa * cb * sy;
        double z = ca * cb * sy - sa * sb * cy;

        return new Quaternion((float)x, (float)y, (float)z, (float)w);
    }

    /**
     * Converts to x/y/z rotation euler output, as used by the game
     */
    public static double[] ToIntrinsicXYZEuler(Quaternion q)
    {
        double[] angles = ToEulerAngles(q);
        double temp = angles[0];
        angles[0] = angles[2];
        angles[2] = temp;

        return angles;
    }

    /**
     * Converts to yaw, pitch, roll euler angles.
     */
    public static double[] ToEulerAngles(Quaternion q)
    {
    	double[] angles = new double[3];

        // roll (x-axis rotation)
        double sinr_cosp = +2.0 * (q.w * q.x + q.y * q.z);
        double cosr_cosp = +1.0 - 2.0 * (q.x * q.x + q.y * q.y);
        angles[2] = Math.atan2(sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        double sinp = +2.0 * (q.w * q.y - q.z * q.x);
        if (Math.abs(sinp) >= 1)
            angles[1] = Math.PI / 2 * Math.signum(sinp); // use 90 degrees if out of range
        else
            angles[1] = Math.asin(sinp);

        // yaw (z-axis rotation)
        double siny_cosp = +2.0 * (q.w * q.z + q.x * q.y);
        double cosy_cosp = +1.0 - 2.0 * (q.y * q.y + q.z * q.z);  
        angles[0] = Math.atan2(siny_cosp, cosy_cosp);

        return angles;
    }

    // yaw pitch roll (zyx) but better and complementary to ToQuaternion
    public static double[] ToEulerAngles2(Quaternion q) {
        q = q.normalise(null);

        double sqw = q.w * q.w;
        double sqx = q.x * q.x;
        double sqy = q.y * q.y;
        double sqz = q.z * q.z;

        double eul[] = new double[3];

        eul[2] = Math.atan2(2 * (q.y*q.z + q.w*q.x), sqw-sqx-sqy+sqz);
        eul[1] = Math.asin(-2 * (q.x*q.z - q.w*q.y));
        eul[0] = Math.atan2(2 * (q.x*q.y + q.w*q.z), sqw+sqx-sqy-sqz);
        return eul;
    }

    public static double[] MatrixToEuler(float[] matrix) {
        Quaternion q = new Quaternion();
        Matrix4f mat = new Matrix4f();
        mat.m00 = matrix[0];
        mat.m01 = matrix[1];
        mat.m02 = matrix[2];
        mat.m03 = matrix[3];
        mat.m10 = matrix[4];
        mat.m11 = matrix[5];
        mat.m12 = matrix[6];
        mat.m13 = matrix[7];
        mat.m20 = matrix[8];
        mat.m21 = matrix[9];
        mat.m22 = matrix[10];
        mat.m23 = matrix[11];
        mat.m30 = matrix[12];
        mat.m31 = matrix[13];
        mat.m32 = matrix[14];
        mat.m33 = matrix[15];
        Quaternion.setFromMatrix(mat, q);

        
        return QUtil.ToIntrinsicXYZEuler(q);
    }

    /**
     * Converts a quaternion to an axis and an angle theta
     * @param axis the rotation axis of q, set by this function
     * @param q the quaternion to convert
     * @return theta, or NaN if not applicable
     */
    public static double ToAxisAngle(float[] axis, Quaternion q) {
        double x = q.x, y = q.y, z = q.z, w = q.w;

        double Nq = w * w + x * x + y * y + z * z;
        if (!Double.isFinite(Nq)) {
            axis[0] = 1; axis[1] = 0; axis[2] = 0;
            return Float.NaN;
        }

        if (Nq < EPSILON) {
            // results are probably unreliable after normalization
            axis[0] = 1; axis[1] = 0; axis[2] = 0;
            return 0;
        }

        if (Nq != 1) {
            // need to normalize
            double s = Math.sqrt(Nq);
            w = w / s; x = x / s; y = y / s; z = z / s;
        }

        double len2 = x * x + y * y + z * z;
        if (len2 < EPSILON) {
            // nearly 0, 0, 0 => this is an identity rotation
            axis[0] = 1; axis[1] = 0; axis[2] = 0;
            return 0;
        }

        double len = Math.sqrt(len2);
        axis[0] = (float) (x / len);
        axis[1] = (float) (y / len);
        axis[2] = (float) (z / len);

        // clamp w to prevent floating point errors
        w = Math.max(Math.min(w, 1), -1);
        return 2 * Math.acos(w);
    }

    public static Quaternion AxisAngleToQuat(float[] axis, double theta)
    {
        Quaternion q = new Quaternion();

        double s = Math.sin(theta/2);
        q.x = (float) (axis[0] * s);
        q.y = (float) (axis[1] * s);
        q.z = (float) (axis[2] * s);
        q.w = (float) Math.cos(theta/2);

        return q;
    }

    public static double[] AxisAngleToIntrinsicXYZEuler(float[] axis, double theta)
    {
        Quaternion q = AxisAngleToQuat(axis, theta);
        return ToIntrinsicXYZEuler(q);
    }

    public static double IntrinsicXYZToAxisAngle(double alpha, double beta, double gamma, float[] axis) {
        Quaternion q = IntrinsicXYZToQuaternion(alpha, beta, gamma);
        return ToAxisAngle(axis, q);
    }


    public enum QuaternionAxis { None, W, X, Y, Z }

    public static float getQuaternionComponent(Quaternion q, QuaternionAxis component) {
        switch (component) {
            case X: return q.x;
            case Y: return q.y;
            case Z: return q.z;
            case W: return q.w;
            default: throw new IllegalArgumentException("Component is not a valid quaternion component.");
        }
    }

    public static void setQuaternionComponent(Quaternion q, QuaternionAxis component, float value) {
        switch (component) {
            case X: q.x = value; break;
            case Y: q.y = value; break;
            case Z: q.z = value; break;
            case W: q.w = value; break;
            default: throw new IllegalArgumentException("Component is not a valid quaternion component.");
        }
    }

    public static Quaternion normalizeOthers(Quaternion q, QuaternionAxis constant) {
        return normalizeOthers(q, constant, QuaternionAxis.None);
    }

    public static Quaternion normalizeOthers(Quaternion q, QuaternionAxis constant, QuaternionAxis modifyPreference) {
        return normalizeOthers(q, constant, QuaternionAxis.None, false);
    }

    public static Quaternion normalizeOthers(Quaternion q, QuaternionAxis constant, QuaternionAxis modifyPreference, boolean negativePreference) {
        // clone the quaternion passed
        q = new Quaternion(q);
        // c = sqrt(1 - clamp0to1(const^2))
        float constVal = getQuaternionComponent(q, constant);
        double c = Math.sqrt(1 - Math.min(Math.pow(constVal, 2), 1));
        double mag = Math.abs(q.x) + Math.abs(q.y) + Math.abs(q.z) + Math.abs(q.w);

        if (mag == 0) {
            return q.setIdentity();
        } else if (constant == QuaternionAxis.None) {
            return q.normalise(q);
        } else if (modifyPreference == constant) {
            return q;
        }

        float v1, v2, v3;
        // Are other values than the constant zeroes?
        if (mag - Math.abs(constVal) == 0) {
            float u = negativePreference ? -1 : 1;
            switch (modifyPreference) {
                case X: q.set(u, 0, 0, 0); break;
                case Y: q.set(0, u, 0, 0); break;
                case Z: q.set(0, 0, u, 0); break;
                case W: q.set(0, 0, 0, u); break;
                case None: q.set(u, u, u, u); break;
            }
            // restore constant
            setQuaternionComponent(q, constant, constVal);
        }

        // anonymize the other values
        switch (constant) {
            case X: v1 = q.y; v2 = q.z; v3 = q.w; break;
            case Y: v1 = q.x; v2 = q.z; v3 = q.w; break;
            case Z: v1 = q.x; v2 = q.y; v3 = q.w; break;
            case W: v1 = q.x; v2 = q.y; v3 = q.z; break;
            default: return null;   // impossible
        }

        // length of v1, v2, v3
        double len = Math.sqrt(Math.pow(v1, 2) + Math.pow(v2, 2) + Math.pow(v3, 2));
        v1 = (float)(c * v1/len);
        v2 = (float)(c * v2/len);
        v3 = (float)(c * v3/len);

        switch (constant) {
            case X: return new Quaternion(q.x, v1, v2, v3);
            case Y: return new Quaternion(v1, q.y, v2, v3);
            case Z: return new Quaternion(v1, v2, q.z, v3);
            case W: return new Quaternion(v1, v2, v3, q.w);
            default: return null;   // again, impossible
        }
    }
}
