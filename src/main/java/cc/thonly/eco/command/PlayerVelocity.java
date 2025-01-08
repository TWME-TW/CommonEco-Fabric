package cc.thonly.eco.command;

public class PlayerVelocity {

    public static int getMathPosition(double yaw, double pitch) {
        int i = 0;
        try {
            PlayerVelocity.var1 position1 = PlayerVelocity.var1.getPosition(yaw);
            PlayerVelocity.var2 position2 = PlayerVelocity.var2.getPosition(pitch);
            if(position2!=null) i = position2.id;
            else i = position1.id;
//            System.out.println("debug code " + i);
            return i;
        } catch (Exception e) {
            i = 0;
            return i;
        }
    }

    public static String getKey(double yaw, double pitch) {
        String string_x;
        try {
            PlayerVelocity.var1 position1 = PlayerVelocity.var1.getPosition(yaw);
            PlayerVelocity.var2 position2 = PlayerVelocity.var2.getPosition(pitch);
            if(position2!=null) string_x = position1.path;
            else string_x = position1.path;
            return string_x;
        } catch (Exception e) {
            string_x = "null";
            return string_x;
        }
    }

    public static enum var1 {
        NORTH(-1000,-10000, 3, "北"),
        NORTH1(-135, -180, 3, "北"),
        NORTH2(180, 135, 3, "北"),
        SOUTH(45, -45, 4, "南"),
        EAST(-45, -135, 1, "东"),
        WEST(135, 45, 2, "西");

        public final double max;
        public final double min;
        public final int id;
        public final String path;

        var1(double max, double min, int id, String path) {
            this.max = max;
            this.min = min;
            this.id = id;
            this.path = path;
        }

        public static var1 getPosition(double input) {
            for (var1 position : values()) {
                if (input >= position.min && input <= position.max) {
                    if(position==NORTH1 || position == NORTH2) position = NORTH;
                    return position;
                }
            }
            return null;
        }
    }
    public static enum var2 {
        UP(-60,-90, 5, "上"),
        DOWN(90,60, 6, "下");

        public final double max;
        public final double min;
        public final int id;
        public final String path;

        var2(double max_x, double min_x, int id, String path) {
            this.max = max_x;
            this.min = min_x;
            this.id = id;
            this.path = path;
        }
        public static var2 getPosition(double input) {
            for (var2 position : values()) {
                if (input >= position.min && input <= position.max) {
                    return position;
                }
            }
            return null;
        }
    }
}
