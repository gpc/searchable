package searchablebug;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

@SuppressWarnings("rawtypes")
public class JodaLocalDateCompassConverter extends AbstractBasicConverter {
    public static final String NAME = "jodaLocalDateCompassConverter";

    private final DateTimeFormatter dateFormatter = ISODateTimeFormat.date();

    @Override
    protected LocalDate doFromString(
            String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext marshallingContext) throws ConversionException
    {
        return dateFormatter.parseDateTime(str).toLocalDate();
    }

    protected String doToString(LocalDate o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return o.toString();
    }

    public static Class getConverterType() {
        return LocalDate.class;
    }
}
