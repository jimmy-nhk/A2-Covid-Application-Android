package com.example.a2.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.example.a2.R;
import com.example.a2.model.Site;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
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
    protected void onBeforeClusterItemRendered(Site site, MarkerOptions markerOptions) {

        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.site_cluster);

        markerOptions.icon(getMarkerIconFromDrawable(clusterIcon));
        markerOptions.title(site.getTitle());
        markerOptions.snippet(site.getSnippet());
        markerOptions.position(new LatLng(site.getLatitude(), site.getLongitude()));

    }


    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<Site> cluster) {
        return cluster.getSize() > 1;
    }








//
//
//    @Override
//    protected int getColor(int clusterSize) {
//        if (clusterSize < 3){
//            return Color.parseColor("#4CFF00");
//        } else if (clusterSize < 5){
//            return Color.parseColor("#00FFFF");
//        } else if (clusterSize < 8){
//            return Color.parseColor("#267F00");
//        } else if (clusterSize < 10){
//            return Color.parseColor("#A17FFF");
//        } else{
//            return Color.parseColor("#7FC9FF");
//        }
//    }
//
//    @Override
//    protected void onClusterItemRendered(Site item, Marker marker){
//        marker.setTitle(item.getName());
//        marker.setTag(item);
//        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));
//        super.onClusterItemRendered(item, marker);
//
//    }
//
//    @Override
//    protected void onClusterItemUpdated(@NonNull Site item, @NonNull Marker marker) {
//        marker.setTitle(item.getName());
//        marker.setTag(item);
//        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.site_cluster));
//    }
//
//
//
//    @Override
//    protected void onClusterRendered(Cluster<Site> cluster, Marker marker) {
//        super.onClusterRendered(cluster, marker);
//        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.site_cluster);
//        marker.setIcon(getMarkerIconFromDrawable(clusterIcon));
//
//        marker.setTitle(marker.getTitle());
//        marker.setSnippet(marker.getSnippet());
//    }
//
//
//
//    @Override
//    protected void onBeforeClusterRendered(Cluster<Site> siteCluster, MarkerOptions markerOptions) {
//
//
//
//        final Drawable clusterIcon = context.getResources().getDrawable(R.drawable.ic_android);
//        clusterIcon.setColorFilter(getColor(siteCluster.getSize()), PorterDuff.Mode.MULTIPLY);
//
//        mClusterIconGenerator.setBackground(clusterIcon);
//        mClusterIconGenerator.setContentPadding(100, 40, 0, 0);
////        mClusterIconGenerator.setTextAppearance(R.style.QText);
//
//        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(siteCluster.getSize()));
//
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
////        markerOptions.icon(drawable);
//
//
//    }
//
//
//
//
//

//
//    public void setUpdateMarker (Site oldItem, Site newItem) {
//        Marker marker = getMarker(oldItem);
//        if (marker != null) {
//            marker.setPosition(newItem.getPosition());
//            marker.setTitle(newItem.getTitle());
//        }
//    }

}
