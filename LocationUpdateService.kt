package com.example.food_ordering_talabatek_app_delivery_boy

//import android.app.Service
//import android.content.Intent
import android.media.MediaPlayer
//import android.os.IBinder
import android.provider.Settings
//import android.util.Log

import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.LoadBundleTaskProgress;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SnapshotMetadata;
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


import io.flutter.plugins.firebase.firestore.streamhandler.DocumentSnapshotsStreamHandler
import io.flutter.plugins.firebase.firestore.streamhandler.LoadBundleStreamHandler
import io.flutter.plugins.firebase.firestore.streamhandler.OnTransactionResultListener
import io.flutter.plugins.firebase.firestore.streamhandler.QuerySnapshotsStreamHandler
import io.flutter.plugins.firebase.firestore.streamhandler.SnapshotsInSyncStreamHandler
import io.flutter.plugins.firebase.firestore.streamhandler.TransactionStreamHandler
import io.flutter.plugins.firebase.firestore.utils.ExceptionConverter
//import io.flutter.plugins.firebase.firestore.FirebaseFirestore



import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import java.util.*

class LocationUpdateService : Service() {

    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    //private lateinit val currentUser:FirebaseUser

    private val CHANNEL_ID = "com.example.food_ordering_talabatek_app_delivery_boy"

    

    var counter = 0
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    private val TAG = "LocationUpdateService"

    init {
        firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder().build()
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        requestLocationUpdates()
         
         
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stoptimertask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, RestartBackgroundService::class.java)
        this.sendBroadcast(broadcastIntent)
    }

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                var count = counter++
                if (latitude != 0.0 && longitude != 0.0) {
                    /*Log.d(
                        "Location::",
                        latitude.toString() + ":::" + longitude.toString() + "Count" +
                                count.toString()
                    )*/
                }
            }
        }
        timer!!.schedule(
            timerTask,
            0,
            1000
        )
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest()
        request.setSmallestDisplacement(5.0f)
        request.setInterval(10000)
        request.setFastestInterval(2000)
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val client: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) { // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location: Location = locationResult.getLastLocation()
                    if (location != null) {
                        var currentUser = auth.getCurrentUser()
                         
                        if(currentUser != null){
                            var uid = currentUser.uid
                            Log.d("Location Service", "uid: "+uid)
                            latitude = location.latitude
                            longitude = location.longitude

                            val docRef = firestore.collection("deliveryUsers").document(uid)
                            docRef.get()
                                .addOnSuccessListener { document ->
                                    if (document != null) {
                                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")

                                        val dataLocation = hashMapOf(
                                            "lat" to latitude,
                                            "long" to longitude
                                        ) 

                                        val dataUser = hashMapOf(
                                            "phone" to "${document.getString("phone")}",
                                            "gender" to "${document.getString("gender")}",
                                            "fullName" to "${document.getString("fullName")}",
                                            "status" to "${document.getString("status")}",
                                            "location" to dataLocation
                                        )       

                                        firestore.collection("deliveryUsers").document(uid)
                                        .set(dataUser)
                                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                                    } else {
                                        Log.d(TAG, "No such document")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d(TAG, "get failed with ", exception)
                                }

                            Log.d("Location Service", "location update $location")
                        }

                        
                    }
                }
            }, null)
        }
    }
}
