package nodomain.yalg;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andreas on 10.09.2015.
 */
public class LevelLoader {

    static private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    static PointF readPoint(XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
        String x = parser.getAttributeValue(null, "x");
        String y = parser.getAttributeValue(null, "y");
        parser.nextTag();
        return new PointF(Float.parseFloat(x), Float.parseFloat(y));
    }

    static ColorF readColor(XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
        String r = parser.getAttributeValue(null, "r");
        String g = parser.getAttributeValue(null, "g");
        String b = parser.getAttributeValue(null, "b");
        parser.nextTag();
        return new ColorF(Float.parseFloat(r), Float.parseFloat(g), Float.parseFloat(b));
    }

    static void readMesh(XmlPullParser parser, Refractor refractor) throws XmlPullParserException, IOException {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            refractor.addMeshPoint(readPoint(parser));
        }
    }

    static void readGameObject(XmlPullParser parser, List<GameObject> gameObjects) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "gameobject");
        String type = parser.getAttributeValue(null, "type");

        GameObject obj = null;
        if (type.equals("laser")) {
            obj = new LaserSource();
        }
        else if (type.equals("refractor")) {
            obj = new Refractor();
        }
        else if (type.equals("receptor")) {
            obj = new Refractor();
        }
        else {
            throw new IOException("Unknown object type!");
        }

        gameObjects.add(obj);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("position")) {
                obj.setPosition(readPoint(parser));
            }
            else if (name.equals("orientation")) {
                obj.setDirection(readPoint(parser));
            }
            else if (name.equals("mesh")) {
                readMesh(parser, (Refractor)obj);
            }
            else if (name.equals("texture")) {
                obj.setTextureName(parser.getAttributeValue(null, "file"));
                parser.nextTag();
            } else if (name.equals("refraction")) {
                ((Refractor)obj).setRefractionIndex(Float.parseFloat(parser.getAttributeValue(null, "index")));
                parser.nextTag();
            } else if (name.equals("movable")) {
                obj.setIsMovable(true);
                parser.nextTag();
            } else {
                skip(parser);
            }
        }
    }

    static void readLevel(XmlPullParser parser, List<GameObject> gameObjects) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "level");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("gameobject")) {
                readGameObject(parser, gameObjects);
            } else {
                skip(parser);
            }
        }
    }
    static public List<GameObject> parse(InputStream in) throws XmlPullParserException, IOException {
        List<GameObject> objects = new ArrayList<GameObject>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readLevel(parser, objects);
        } finally {
            in.close();
        }
        return objects;
    }
}
