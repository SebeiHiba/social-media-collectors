package sm.wrapper;
import com.taxonic.carml.engine.function.FnoFunction;
import com.taxonic.carml.engine.function.FnoParam;


public class RMLFunctions {

    public int pars (String str)
    {
        int x = Integer.parseInt(str);
        return x;
    }

    @FnoFunction("http://localhost/socialNetwork/model/YearMappingFunction")
    public String YearMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String year= date.substring(0,4);
        return year ;
    }
    @FnoFunction("http://localhost/socialNetwork/model/MonthMappingFunction")
    public int MonthMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String month=  date.substring(6,7);
        return pars(month);
    }
    @FnoFunction("http://localhost/socialNetwork/model/DayMappingFunction")
    public int DayMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String dayofMonth=  date.substring(8,10);
        return pars(dayofMonth);
    }
    @FnoFunction("http://localhost/socialNetwork/model/HourMappingFunction")
    public int HourMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String hour=  date.substring(11,13);
        return pars (hour) ;
    }
    @FnoFunction("http://localhost/socialNetwork/model/MinuteMappingFunction")
    public int MinuteMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String minute=  date.substring(14,16);
        return pars (minute) ;
    }
    @FnoFunction("http://localhost/socialNetwork/model/SecondMappingFunction")
    public int SecondMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String date)
    {
        String second=  date.substring(17,19);
        return pars (second) ;
    }
    @FnoFunction("http://localhost/socialNetwork/model/OpinionMappingFunction")
    public String OpinionMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String text)
    {
        // add algorithm for opinion mining
        float result = 0;

        if (result==-1){
            return "Negative";
        }
        else if (result==0){
            return  "Neutral";
        }
        else return  "Positive";

    }
    @FnoFunction("http://localhost/socialNetwork/model/TopicMappingFunction")
    public String TopicMappingFunction (@FnoParam("http://localhost/socialNetwork/model/intParameterA") String text)
    {
        // add algorithm for topic detection

        return "Topic" ;
    }
}
