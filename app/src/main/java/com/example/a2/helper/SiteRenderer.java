package com.example.a2.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.example.a2.R;
import com.example.a2.model.Site;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class SiteRenderer extends DefaultClusterRenderer<Site> {

    private Context context;
    private final IconGenerator mClusterIconGenerator;

    public SiteRenderer(Context context, GoogleMap map, ClusterManager<Site> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;

        mClusterIconGenerator = new IconGenerator(context);
    }

    @Override
    protected int getColor(int clusterSize) {
        if (clusterSize < 3){
            return Color.parseColor("#4CFF00");
        } else if (clusterSize < 5){
            return Color.parseColor("#00FFFF");
        } else if (clusterSize < 8){
            return Color.parseColor("#267F00");
        } else if (clusterSize < 10){
            return Color.parseColor("#A17FFF");
        } else{
            return Color.parseColor("#7FC9FF");
        }
    }

    @Override
    protected void onBeforeClusterItemRendered(Site site, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));

//        Drawable drawable = context.getResources().getDrawable(R.drawable.site_cluster) ;
//        markerOptions.icon(getMarkerIconFromDrawable(drawable));
    }

    @Override
    protected void onClusterRendered(Cluster<Site> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));

    }



    @Override
    protected void onBeforeClusterRendered(Cluster<Site> siteCluster, MarkerOptions markerOptions) {


        //TODO: Check again here
        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.site_cluster);
        clusterIcon.setColorFilter(getColor(siteCluster.getSize()), PorterDuff.Mode.MULTIPLY);

        mClusterIconGenerator.setBackground(clusterIcon);
        mClusterIconGenerator.setContentPadding(100, 40, 0, 0);
//        mClusterIconGenerator.setTextAppearance(R.style.QText);

        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(siteCluster.getSize()));

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));
//        markerOptions.icon(drawable);
        markerOptions.title(siteCluster.getClass().getName());

    }

    @Override
    protected void onClusterItemRendered(Site item, Marker marker){
        super.onClusterItemRendered(item, marker);
        marker.setTitle(item.getName());
        marker.setTag(item);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));

    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Site> cluster) {
        return cluster.getSize() > 1;
    }

    public void setUpdateMarker (Site oldItem, Site newItem) {
        Marker marker = getMarker(oldItem);
        if (marker != null) {
            marker.setPosition(newItem.getPosition());
            marker.setTitle(newItem.getTitle());
        }
    }

}
