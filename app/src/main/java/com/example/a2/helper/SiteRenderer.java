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


}
