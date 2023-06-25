public final class Trig {
    // All Inputs and outputs are in Degrees
    public final static double pi = 3.14159265358979323846264338327950288419716939937510582;
    private static final double Radians = pi / 180;
    private static final double Degrees = 180 / pi;
    private static double prevSin = 90;
    private static double prevCos = 90;
    private static double sinx = 1;
    private static double cosx = 0;

    private Trig() { // Utility class for Trig/Math operations, no instances are allowed
        throw new UnsupportedOperationException();
    }
    
    public static double toRad(double degrees) {
        return degrees * Radians;
    } // converts degrees to radians
    public static double toDeg(double radians) {
        return radians * Degrees;
    } // converts radians to degrees

    public static double sin(double degrees) {
        while (degrees > 360) { degrees -= 360; }
        while (degrees < 0) { degrees += 360; }

        if (degrees == prevSin) return sinx;
        prevSin = degrees;

        if (degrees == 0 || degrees == 180) return sinx = 0;
        else if (degrees == 360) return sinx = 0;
        else if (degrees == 90) return sinx = 1;
        else if (degrees == 270) return sinx = -1;

        double x;
        boolean useSin = false, useCosine = false;
        int sign = 1;
        if (degrees > 180 && degrees < 360) {
            sign = -1;
            if (degrees < 270) degrees -= 180;
            else if (degrees > 270) degrees = 360 - degrees;

            if (degrees <= 45) useSin = true;
            else if (degrees > 45) useCosine = true;

        } else if (degrees < 180 && degrees > 0) {
            if (degrees > 90) degrees = 180 - degrees;

            if (degrees <= 45) useSin = true;
            else if (degrees > 45) useCosine = true;
        }

        if (useSin) {
            x = degrees * Radians;
            return sinx = sign * (x -(pow(x, 3)/ 6)+(pow(x, 5)/120));
        } else if (useCosine) {
            x = (90 - degrees) * Radians;
            return sinx = sign * (1-(x*x/2)+(pow(x,4)/24)-(pow(x,6)/720));
        }
        return sinx;
    } // returns the value of sin(degrees)

    public static double cos(double degrees) {
        while (degrees > 360) { degrees -= 360; }
        while (degrees < 0) { degrees += 360; }

        if (degrees == prevCos) return cosx;
        prevCos = degrees;

        if (degrees == 90 || degrees == 270) return cosx = 0;
        else if (degrees == 180) return cosx = -1;
        else if (degrees == 0 || degrees == 360) return cosx = 1;

        double x, sign = 1;
        boolean useSin = false, useCosine = false;
        if (degrees > 90 && degrees < 270) {
            sign = -1;
            if (degrees < 180) degrees = 180 - degrees;
            else if (degrees > 180) degrees -= 180;

            if (degrees <= 45) useCosine = true;
            else if (degrees > 45) useSin = true;
        } else if ((degrees < 90 && degrees > 0) || (degrees > 270 && degrees < 360)) {
            if (degrees > 270) degrees = 360 - degrees;

            if (degrees <= 45) useCosine = true;
            else if (degrees > 45) useSin = true;
        }
        if (useSin) {
            x = (90 - degrees) * Radians;
            return cosx = sign * (x - (pow(x,3)/6) + (pow(x, 5)/120));
        } else if (useCosine) {
            x = degrees * Radians;
            return cosx = sign * (1 - (x*x/2) + (pow(x,4)/24) - (pow(x,6)/720));
        }
        return cosx;
    } // returns the value of cos(degrees)

    public static double tang(double degrees) {
        return sin(degrees)/cos(degrees);
    } // returns the value of tan(degrees)

    public static double arcsin(double x) {
        System.out.println("Yay");
        if (x > 1 || x < -1) return Double.NaN;
        //return x + (pow(x,3)/6) + (3 * pow(x, 5)/40) + (5 * pow(x, 7)/112);
        return arctan(x/(root(1-pow(x,2))));
    } // returns the value of arcsin(x)

    public static double arccos(double x) {
        if (x > 1 || x < -1) return Double.NaN;
        //return (pi/2) - arcsin(x);
        return arctan(root((1-pow(x,2)/x)));
    } // returns the value of arccos(x)

    public static double arctan(double x) {
        double sign = 1;
        double x1;
        if (x < 0) {
            x = -x;
            sign = -1;
        }
        if (x > 1) {
            if (1/x > 0.268) {
                x = pi/6 + arctanPoly((root(3) * 1/x) - 1)/(root(3) + 1/x);
            } else {
                x = arctanPoly(1/x);
            }
            x1 = sign * (pi/2 - x);
        } else {
            if (x > 0.268) {
                x = pi/6 + arctanPoly((root(3) * x) - 1)/(root(3) + x);
            } else {
                x = arctanPoly(x);
            }
            x1 = sign * x;
        }
        return x1 * Degrees;
    } // returns the value of arctan(x)

    private static double arctanPoly(double x) {
        return x - (pow(x,3)/3) + (pow(x,5)/5) - (pow(x,7)/7);
    } // first 4 iterations of the Power series of arctan

    public static double arctan2(double x, double y) {
        if (y == 0 && x == 0) {
            return 0;
        } else if (y < 0 && x == 0)  {
            return -1 * 90;
        } else if (y > 0 && x == 0) {
            return 90;
        } else if (y < 0 && x < 0) {
            return -180 + arctan(y/x);
        } else if (y >= 0 && x < 0) {
            return 180 + arctan(y/x);
        } else if (x > 0) {
            return arctan(y/x);
        } else return 0;
    } // retruns the value of arctan2(x, y)

    public static double pow(double x, double n) {
        double power = x;
        for (int i = 1; i < n; i++)
            power *= x;
        return power;
    } // returns x^n calculated iteratively

    public static double root(double n) {
        if (n < 0) return Double.NaN;
        else if (n == 1) return 1;
        double x1 = n;
        double x2 = Math.random() * n/2;
        while (Math.abs(x1 - x2) > 0.00001) {
            x1 = rootFormula(x2, n);
            x2 = rootFormula(x1, n);
        }
        return x1;
    } // calculates the square root of n

    public static double getAverage(double ...x) {
        double sum = 0;
        for (int i = 0; i < x.length; i++)
            sum += x[i];
        return sum / x.length;
    } // returns the average of an array of doubles

    public static double getAverage(Vec3 v) {
        double sum = v.x;
        sum += v.y; sum += v.z;
        return sum / 3;
    } // returns the average of the components in a 3d vector

    private static double rootFormula(double x, double n) {
        return (x/2) + (n/(2*x));
    } // helper function for calculating the square root

    public static int fact(int n) {
        return n == 0 ? 1 : n * fact(n - 1);
    } // calculate n! using recursion

    public static int signOf(double n) {
        return n == 0 ? 0 : (n > 0 ? 1 : -1);
    } // returns the sign of n

    public static int randomSign() { return Math.random() > 0.5 ? -1 : 1; } // randomly returns a 1 or -1

    // returns a random int from [min, max)
    public static int randomInt(int max, int min) { return (int) (Math.random() * (max - min)) + min; } 
    // returns a random int from [0, max)
    public static int randomInt(int max) { return (int) (Math.random() * max); }
    // returns a random double from [min, max]
    public static double randomDouble(double max, double min) { return (Math.random() * (max - min)) + min; }
    // returns a random double from [0, max]
    public static double randomDouble(double max) { return Math.random() * max; }
    // rounds a double to nearest whole number
    public static double round(double n) {
        double decimal = n - (int) (n);
        return decimal >= 0.5 ? n + (1 - decimal) : n - decimal;
    }
    // returns the minimum of two doubles
    public static double min(double a, double b) { return a > b ? b : a; }
    // returns the maximum of two doubles
    public static double max(double a, double b) { return a > b ? a : b; }
    // returns the minimum of two ints
    public static int min(int a, int b) { return a > b ? b : a; }
    // returns the maximum of two ints
    public static int max(int a, int b) { return a > b ? a : b; }
}
