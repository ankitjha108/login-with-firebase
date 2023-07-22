package com.example.loginwithfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.loginwithfirebase.databinding.ActivityLoginBinding
import com.example.loginwithfirebase.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import com.google.firebase.ktx.initialize
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var etEmail: EditText
    lateinit var etConfPass: EditText
    private lateinit var etPass: EditText
    private lateinit var btnSignUp: Button
    lateinit var tvRedirectLogin: TextView
    private lateinit var verificationIdFromFirebase: String // Define the variable here
    // we will use this to match the sent otp from firebase
    lateinit var storedVerificationId:String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    // create Firebase authentication object
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
                binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Initialize Firebase with the logging level set to ERROR
        Firebase.initialize(applicationContext)

        // View Bindings
        etEmail = findViewById(R.id.etSEmailAddress)
        etConfPass = findViewById(R.id.etSConfPassword)
        etPass = findViewById(R.id.etSPassword)
        btnSignUp = findViewById(R.id.btnSSigned)
        tvRedirectLogin = findViewById(R.id.tvRedirectLogin)

        // Initializing auth object
        auth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener {
            signUpUser()
        }

        binding.verify.setOnClickListener {
            val phoneNumber = binding.etPhone.text.toString().trim()
            if (phoneNumber.isNotEmpty()) {
                // Format the phone number to E.164 format for India
                val formattedPhoneNumber = formatPhoneNumberForIndia(phoneNumber)

                // Call the method to send the verification code to the formatted phone number
                sendVerificationCode(formattedPhoneNumber)
            } else {
                // Show error message for invalid phone number
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            }
        }

       /* binding.Submit.setOnClickListener {
            val otp = binding.OptVerify.text?.trim().toString()
            if (otp.isNotEmpty()) {
                // Get the PhoneAuthCredential object using the entered OTP
                val credential: PhoneAuthCredential =
                    PhoneAuthProvider.getCredential(binding.etPhone.text.toString().trim(), otp)


                // Sign in with the provided PhoneAuthCredential
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
*/
        /* binding.verify.setOnClickListener {
             val phoneNumber=binding.etPhone.text.toString().trim()
             if (binding.etPhone.toString().isNotEmpty()) {
                 // Format the phone number to E.164 format
                 val formattedPhoneNumber = formatPhoneNumberForIndia(phoneNumber)

                 // Call the method to send the verification code to the formatted phone number
                 sendVerificationCode(formattedPhoneNumber)
                 // Call the method to send the verification code to the user's phone number
                 sendVerificationCode(phoneNumber)
             } else {
                 // Show error message for invalid phone number
                 Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT)
             }

         }*/
       binding.Submit.setOnClickListener {
            val otp = binding.OptVerify.text?.trim().toString()
            if(otp.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential( storedVerificationId, otp)
                signInWithPhoneAuthCredential(credential)
            }else{
                Toast.makeText(this,"Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // switching from signUp Activity to Login Activity
        tvRedirectLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
    private fun formatPhoneNumberForIndia(phoneNumber: String): String {
        return PhoneNumberUtils.formatNumberToE164(phoneNumber, "IN") ?: phoneNumber
    }

    private fun signUpUser() {
        val email = etEmail.text.toString()
        val pass = etPass.text.toString()
        val confirmPassword = etConfPass.text.toString()

        // check pass
        if (email.isBlank() || pass.isBlank() || confirmPassword.isBlank()) {
            Toast.makeText(this, "Email and Password can't be blank", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPassword) {
            Toast.makeText(this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT)
                .show()
            return
        }
        // If all credential are correct
        // We call createUserWithEmailAndPassword
        // using auth object and pass the
        // email and pass in it.
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }


    }
    // ... View Bindings and initialization ...



// ... Other functions ...

private fun sendVerificationCode(phoneNumber: String) {
    PhoneAuthProvider.getInstance().verifyPhoneNumber(
        phoneNumber,
        60, // Timeout duration in seconds
        TimeUnit.SECONDS,
        this, // Activity or fragment instance

        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()

    {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback is invoked in two situations:
                // 1. Auto-retrieval of SMS code has completed.
                // 2. User has manually verified the code.
                // You can handle auto-verification here if needed.
                Log.d("GFG" , "onVerificationCompleted Success")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Handle verification failure, e.g., invalid phone number or quota exceeded
                // Show error message to the user
                Log.d("GFG" , "onVerificationFailed  $e")
                Toast.makeText(this@MainActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("GFG","onCodeSent: $verificationId")
                storedVerificationId = verificationId
                resendToken = token
                // Save the verificationId and token to be used for verifying the OTP
                // Display the OTP input screen to the user
                // You can pass the verificationId and token to the next activity or fragment
                // using Intent or arguments
                binding.OptVerify.visibility = View.VISIBLE
                binding.Submit.visibility = View.VISIBLE
                binding.verify.visibility = View.GONE

                // Save the verificationId and use it to complete the verification
                // when the user enters the OTP
                verificationIdFromFirebase = verificationId
            }
        }
    )
}


// ... Other helper functions ...

   /* private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60, // Timeout duration in seconds
            TimeUnit.SECONDS,
            this, // Activity or fragment instance
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback is invoked in two situations:
                    // 1. Auto-retrieval of SMS code has completed.
                    // 2. User has manually verified the code.
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure, e.g., invalid phone number or quota exceeded
                    // Show error message to the user
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    binding.OptVerify.visibility = View.VISIBLE
                    binding.Submit.visibility = View.VISIBLE
                    binding.verify.visibility = View.GONE
                    // Save the verificationId and token to be used for verifying the OTP
                    // Display the OTP input screen to the user
                    // You can pass the verificationId and token to the next activity or fragment
                    // using Intent or arguments
                }
            }
        )
    }*/

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,"Successfully Signed In", Toast.LENGTH_SHORT).show()
                    // Sign-in success, navigate to the main screen or perform other actions
                    // The user is now signed in with phone number
                } else {
                    Toast.makeText(this,"Invalid OTP", Toast.LENGTH_SHORT).show()
                    // Sign-in failed, display an error message
                    // You can handle specific error cases, e.g., invalid code, expired code, etc.
                }
            }
    }

    // Add your code for handling OTP input and verification here
    // ...



}
   /* private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60, // Timeout duration in seconds
            TimeUnit.SECONDS,
            this, // Activity or fragment instance
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This callback is invoked in two situations:
                    // 1. Auto-retrieval of SMS code has completed.
                    // 2. User has manually verified the code.
                    signInWithPhoneAuthCredential(credential)
                    binding.OptVerify.visibility = View.VISIBLE

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // Handle verification failure
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // Save the verificationId and show the OTP entry screen to the user
                    // Pass the verificationId and token to the next activity or fragment
                    // where the user can enter the OTP.
                }
            }
        )
    }*/


