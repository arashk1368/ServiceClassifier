package cloudservices.brokerage.serviceclassification;

//http://marf.cvs.sourceforge.net/viewvc/marf/marf/src/marf/Storage/Result.java?revision=1.22&view=markup
import marf.Storage.Result;

// http://marf.cvs.sourceforge.net/viewvc/marf/marf/src/marf/Storage/ResultSet.java?revision=1.24&view=markup
import marf.Storage.ResultSet;

public class MTest {

    public static void main(String[] argv)
            throws Exception {
        if (argv.length > 0) {
            new MTest().runMARFTest(argv);
        } else {
            String trainAddress = "train-WSDLS.xml";
            String testAddress = "test-WSDLS.xml";
//            String command = "--train";
            String command="--batch-ident";
//            String command="--stats";

            String settings = "-noclustering -dynaclass -silence -endp -lpc -cheb -cve";
            // All the typical options should be there, --batch-ident, --nlp, -dynaclass, -raw, -fft, -cos etc.
            // with a prefix and the index filenames
            new MTest().runMARFTest(new String[]{command, "-noclustering", "-dynaclass", "-silence", "-endp", "-lpc", "-cheb",
                trainAddress, testAddress});
        }
    }

    public void runMARFTest(String[] argv)
            throws Exception {
        marf.apps.MARFCAT.MARFCATApp oApp = new marf.apps.MARFCAT.MARFCATApp();
        oApp.process(argv);

        int iIdentifiedID = 0;
        int iSecondClosestID = 0;

        //ResultSet oResultSet = oApp.getResultSet().getResultSetSorted();
        ResultSet oResultSet = oApp.getResultSet();
        
        System.out.println("**********************************");
        
        for (Result oResult : oResultSet.getResultSetSorted()) {
            // First guess
            iIdentifiedID = oResult.getID();

            // Second guess
            Result oSecondResult = null;

            if (oResultSet.size() > 1) {
                // Second best
                oSecondResult = oResultSet.getSecondClosestResult();
                iSecondClosestID = oSecondResult.getID();
            }

            System.out.println("Subject's ID: " + iIdentifiedID + "\n");
            System.out.println("Subject identified: " + oApp.getDBPerConfig().getName(iIdentifiedID) + "\n");

            System.out.println(oResult);
        }

////         Get a Report data structure, with Warning collection
//        System.out.println(oApp.getReport());
////         Get Configuration object if wanted
//        System.out.println(oApp.getConfiguration());
    }
}
