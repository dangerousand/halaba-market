package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: MainViewModel) {
    val isAmharic = viewModel.selectedLanguage == "Amharic"
    
    // authMode: 0 = Log In, 1 = Register, 2 = Reset Password
    var authMode by remember { mutableStateOf(0) }
    
    var fullName by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    var userKebele by remember { mutableStateOf("Kebele 01") }
    
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    
    val kebelesList = listOf("Kebele 01", "Kebele 02", "Kebele 03", "Kebele 04", "Kebele 05")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "🌶️",
                    fontSize = 44.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = if (isAmharic) "የሀላባ ገበያ" else "HALABA MARKET",
                    fontSize = 24.sp,
                    color = HalabaDarkGreen,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = if (isAmharic) "ደህንነቱ የተጠበቀ የአካባቢ ግንኙነት በሀላባ ከተማ" else "Secure local connection within Halaba City",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Input Form Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // customized Toggle Header (Log In / Register / Reset)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (authMode == 0) HalabaDarkGreen else Color.Transparent)
                            .clickable {
                                authMode = 0
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAmharic) "ግባ" else "Log In",
                            color = if (authMode == 0) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (authMode == 1) HalabaDarkGreen else Color.Transparent)
                            .clickable {
                                authMode = 1
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAmharic) "ተመዝገብ" else "Register",
                            color = if (authMode == 1) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (authMode == 2) HalabaDarkGreen else Color.Transparent)
                            .clickable {
                                authMode = 2
                                errorMessage = ""
                                successMessage = ""
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAmharic) "ቀይር" else "Reset Pass",
                            color = if (authMode == 2) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error / Success Banner
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = HalabaCrimson,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Dynamic Form content
                when (authMode) {
                    0 -> {
                        // LOG IN FORM
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text(if (isAmharic) "የኢሜይል አድራሻ" else "Email Address") },
                            placeholder = { Text("email@example.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userPassword,
                            onValueChange = { userPassword = it },
                            label = { Text(if (isAmharic) "የይለፍ ቃል" else "Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (emailAddress.isEmpty() || !emailAddress.contains("@")) {
                                    errorMessage = if (isAmharic) "እባክዎ ትክክለኛ የኢሜይል አድራሻ ያስገቡ።" else "Please enter a valid email address."
                                } else if (userPassword.length < 4) {
                                    errorMessage = if (isAmharic) "የይለፍ ቃል ቢያንስ 4 ቁምፊዎች መሆን አለበት።" else "Password must be at least 4 characters."
                                } else {
                                    errorMessage = ""
                                    viewModel.loginUser(emailAddress, userPassword) { success, msg ->
                                        if (!success) {
                                            errorMessage = msg
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "ግባ" else "Log In",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                        }
                    }
                    1 -> {
                        // REGISTRATION FORM
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text(if (isAmharic) "ሙሉ ስም" else "Full Name") },
                            placeholder = { Text(if (isAmharic) "ሙሉ ስም ያስገቡ" else "e.g. John Doe") },
                            leadingIcon = { Icon(Icons.Default.Person, "Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text(if (isAmharic) "የኢሜይል አድራሻ" else "Email Address") },
                            placeholder = { Text("name@example.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text(if (isAmharic) "የስልክ ቁጥር" else "Phone Number") },
                            placeholder = { Text("0912345678") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, "Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userPassword,
                            onValueChange = { userPassword = it },
                            label = { Text(if (isAmharic) "የይለፍ ቃል" else "Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Kebele Selector Chips
                        Text(
                            text = if (isAmharic) "ሰፈር ይምረጡ (የሀላባ ቀበሌ) " else "Select Kebele (Halaba City)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextGray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            kebelesList.forEach { keb ->
                                val isSel = userKebele == keb
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) HalabaDarkGreen else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { userKebele = keb }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = keb,
                                        fontSize = 11.sp,
                                        color = if (isSel) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (fullName.length < 2) {
                                    errorMessage = if (isAmharic) "እባክዎ ሙሉ ስምዎን ያስገቡ።" else "Please enter your full name."
                                } else if (emailAddress.isEmpty() || !emailAddress.contains("@")) {
                                    errorMessage = if (isAmharic) "እባክዎ ትክክለኛ የኢሜይል አድራሻ ያስገቡ።" else "Please enter a valid email address."
                                } else if (phoneNumber.length < 9) {
                                    errorMessage = if (isAmharic) "እባክዎ ትክክለኛ የስልክ ቁጥር ያስገቡ።" else "Please enter a valid phone number."
                                } else if (userPassword.length < 4) {
                                    errorMessage = if (isAmharic) "የይለፍ ቃል ቢያንስ 4 ቁምፊዎች መሆን አለበት።" else "Password must be at least 4 characters."
                                } else {
                                    errorMessage = ""
                                    viewModel.registerUser(
                                        fullName,
                                        emailAddress,
                                        phoneNumber,
                                        userPassword,
                                        userKebele
                                    ) { success, msg ->
                                        if (!success) errorMessage = msg
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HalabaSoftGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "ይመዝገቡ" else "Register Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                        }
                    }
                    2 -> {
                        // RESET PASSWORD FORM
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text(if (isAmharic) "የተመዘገበ ኢሜይል" else "Registered Email") },
                            placeholder = { Text("email@example.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, "Email") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userPassword,
                            onValueChange = { userPassword = it },
                            label = { Text(if (isAmharic) "አዲስ የይለፍ ቃል" else "New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, "Password") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (emailAddress.isEmpty() || !emailAddress.contains("@")) {
                                    errorMessage = if (isAmharic) "እባክዎ ትክክለኛ የኢሜይል አድራሻ ያስገቡ።" else "Please enter a valid email address."
                                } else if (userPassword.length < 4) {
                                    errorMessage = if (isAmharic) "አዲሱ የይለፍ ቃል ቢያንስ 4 ቁምፊዎች መሆን አለበት።" else "New password must be at least 4 characters."
                                } else {
                                    errorMessage = ""
                                    successMessage = ""
                                    viewModel.resetPassword(emailAddress, userPassword) { success, msg ->
                                        if (success) {
                                            successMessage = msg
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HalabaDarkGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = if (isAmharic) "የይለፍ ቃል ቀይር" else "Update Password",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )
                        }
                    }
                }
            }

            // Quick Testing Actions & Continuing
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isAmharic) "ወይም በፍጥነት ይግቡ (ለሙከራ)፡" else "Or access instantly for testing:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Quick role access for testing (we can directly map to preseeded demo accounts)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    RoleQuickButton(label = if (isAmharic) "ገዢ (ሙከራ)" else "Buyer Demo", emoji = "👤") {
                        viewModel.currentUserId = "buyer_1"
                        viewModel.currentScreenRoute = "home"
                    }
                    RoleQuickButton(label = if (isAmharic) "ሻጭ (ሙከራ)" else "Seller Demo", emoji = "📦") {
                        viewModel.currentUserId = "seller_1"
                        viewModel.currentScreenRoute = "home"
                    }
                    RoleQuickButton(label = if (isAmharic) "አስተዳዳሪ" else "Admin", emoji = "🛡️") {
                        viewModel.currentUserId = "admin_1"
                        viewModel.currentScreenRoute = "home"
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        // Enter as Guest
                        viewModel.currentUserId = "buyer_1"
                        viewModel.currentScreenRoute = "home"
                    }
                ) {
                    Text(
                        text = if (isAmharic) "በእንግዳነት ቀጥል" else "Continue as Guest",
                        fontSize = 13.sp,
                        color = HalabaDarkGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Guest",
                        tint = HalabaDarkGreen,
                        modifier = Modifier.size(16.dp).padding(start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RoleQuickButton(label: String, emoji: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
        leadingIcon = { Text(emoji, fontSize = 14.sp) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = HalabaDarkGreen
        )
    )
}
