package florian.com.areacalculator;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Florian on 08-11-14.
 */
public class GeoUtils {
    static final double R = 6371.0;

    /*public static Point computePoint(double angle, double d, Point p){
        double dist = d/R;
        double brng = Math.toRadians(angle);
        double lat1 = Math.toRadians(p.getLat());
        double lon1 = Math.toRadians(p.getLon());

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
        double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
        System.out.println("a = " +  a);
        double lon2 = lon1 + a;

        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;

        return new Point(Math.toDegrees(lat2),Math.toDegrees(lon2));
    }*/

    public static double computeArea(ArrayList<LatLng>list){
        ArrayList<Double> listE = new ArrayList<>();
        ArrayList<Double> listN = new ArrayList<>();

        //projection des coordonnées
        //UTM
        //http://fr.wikipedia.org/wiki/Transverse_Universelle_de_Mercator
        int i,j;

        double vLat, A, sLat, T, C, k0, E, N,e=0.0818192,a=6378137, longitude, latitude;
        for (i = 0; i < list.size(); i++)
        {
            double N0;
            if (list.get(i).latitude >= 0)
            {
                //hémisphère nord
                N0 = 0;
            }
            else
            {
                //hémisphère sud
                N0 = 10000;
            }

				/*
                //Convertion degrés décimaux en degrés minute
                longitude=degreeDecimalToDegreeMinutes( listLocation.ElementAt(i).Longitude);
                latitude= degreeDecimalToDegreeMinutes(listLocation.ElementAt(i).Latitude);
				 */

            //convertion des angles en radian
            longitude = Math.toRadians(list.get(i).longitude);
            latitude = Math.toRadians(list.get(i).latitude);

            //Définition de la longitude du méridien de référence ici NUtm = 31 car la Belgique est situé dans la zone UTM numéro 31
            int NUtm = (int)longitude / 6;
            if (NUtm < 0)
            {
                NUtm = 30 + NUtm;
            }
            else
            {
                NUtm = 30 + NUtm + 1;
            }
            double lo0 = Math.toRadians((3+(6*(NUtm-1))-180));


            //calcul des variables intermédiaires
            vLat = (1 / Math.sqrt(1 - Math.pow(e, 2) * Math.pow(Math.sin(latitude), 2)));
            A = (longitude - lo0) * Math.cos(latitude);
            sLat = (1 - (Math.pow(e, 2) / 4) - ((3 * Math.pow(e, 4)) / 64) - (5 * Math.pow(e, 6) / 256)) * latitude - ((3 * Math.pow(e, 2) / 8) + (3 * Math.pow(e, 4) / 32) + (45 * Math.pow(e, 6) / 1024)) * Math.sin(2 * latitude) + ((15 * Math.pow(e, 4) / 256) + (45 * Math.pow(e, 6) / 1024)) * Math.sin(4 * latitude) - (35 * Math.pow(e, 6) / 3072) * Math.sin(6 * latitude);
            T = Math.pow(Math.tan(latitude), 2);
            C = (Math.pow(e, 2) / (1 - Math.pow(e, 2))) * Math.pow(Math.cos(latitude), 2);
            k0 = 0.9996;
            E = 500 + k0 * a * vLat * (A + (1 - T + C) * (Math.pow(A, 3) / 6) + (5 - 18 * T + Math.pow(T, 2)) * (Math.pow(A, 5) / 120));

            N = N0 + k0 * a * (sLat + vLat * Math.tan(latitude) * ((Math.pow(A, 2) / 2) + (5 - T + 9 * C + (4 * Math.pow(C, 2))) * (Math.pow(A, 4) / 24) + (61 - 58 * T + Math.pow(T, 2)) * (Math.pow(A, 6) / 720)));

            listE.add(E);
            listN.add(N);
        }


        //calcul surface polygone
        int Nb = listE.size();

        double area = 0;

        for (i = 0; i < Nb; i++)
        {
            j = (i + 1) % Nb;
            area += listE.get(i) * listN.get(j);
            area -= listN.get(i) * listE.get(j);
        }
        area /= 2.0;
        area = Math.abs(area);

        return area;
    }
}
