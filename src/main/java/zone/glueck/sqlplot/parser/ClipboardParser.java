package zone.glueck.sqlplot.parser;

import zone.glueck.sqlplot.sql.DataSource;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zach on 10/23/16.
 */
public class ClipboardParser implements DataSource {

    private final List<String[]> data = new ArrayList<>();

    private final List<String> headers = new ArrayList<>();

    public static final Pattern LINE_DELIM = Pattern.compile("\n");

    public static final Pattern COLUMN_DELIM = Pattern.compile(",");

    private ClipboardParser() {

    }

    public static ClipboardParser parse() {

        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Clipboard clipboard = toolkit.getSystemClipboard();
            String result = (String) clipboard.getData(DataFlavor.stringFlavor);
            ClipboardParser clipboardParser = new ClipboardParser();
            clipboardParser.parseData(result);
            return clipboardParser;
        } catch (Exception e ) {
            e.printStackTrace();
            return null;
        }

    }

    private void parseData(String data) {

        String[] lines = LINE_DELIM.split(data);

        boolean first = true;
        for (String line : lines) {
            if (first) {
                // Check for alpha characters indicating a header
                first = false;
                if (Character.isAlphabetic(line.codePointAt(0))) {
                    String[] headers = COLUMN_DELIM.split(line);
                    for (String header : headers) {
                        this.headers.add(header.trim());
                    }
                } else {
                    this.data.add(COLUMN_DELIM.split(line));
                }
                continue;
            }
            String[] columns = COLUMN_DELIM.split(line);
            this.data.add(columns);
        }

    }

    @Override
    public List<String> getColumnNames() {
        if (this.headers.isEmpty()) {
            // need to provide the default pattern
            for (int i = 0; i < this.data.get(0).length ; i++) {
                this.headers.add(String.format("Column%02d", i));
            }
        }

        return this.headers;
    }

    @Override
    public int getColumnCount() {
        return this.data.get(0).length;
    }

    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public String getValueAt(int row, int column) {
        if (row > 0 && column > 0 && row < this.data.size() && column < this.data.get(0).length) {
            return this.data.get(row)[column];
        }
        return null;
    }
}
