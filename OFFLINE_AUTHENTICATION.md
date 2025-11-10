# Offline Authentication Feature

## Overview

This app now supports offline authentication, allowing users to continue using the app even when there's no internet connection. This feature complements the existing Auth0 authentication system.

## How It Works

### 1. First Time Setup (Online)

When a user logs in with Auth0 for the first time (or if they haven't set up offline access):

1. User logs in normally via Auth0 OAuth flow
2. After successful login, they're prompted to set up an offline password
3. The offline password is securely stored locally using encrypted SharedPreferences
4. A hash of (email + offline_password) is created and stored securely

### 2. Offline Login

When the user is offline and tries to access the app:

1. The app detects no internet connection
2. If offline credentials are set up:
   - Shows `OfflineLoginScreen` with email and password fields
   - User enters their email and offline password
   - Credentials are validated against the stored hash
   - On success, user enters the app in offline mode
3. If no offline credentials are set up:
   - Shows `NoOfflineAccessScreen` informing user they need to set up offline access first

### 3. Connection Restored

When internet connection is restored while a user is logged in via offline mode:

1. The app detects the connection restoration
2. Shows `ReLoginPromptDialog` asking the user to log in normally
3. User can choose to:
   - Log in now via Auth0
   - Dismiss and continue in offline mode (will be prompted again)

### 4. Offline Mode Capabilities

In offline mode, users have a limited user profile:
- User ID: `offline_{email}`
- Name: Extracted from email (part before @)
- Email: The stored email
- Picture: Empty string

## Security Features

### Encrypted Storage
- Uses `androidx.security:security-crypto` library
- Credentials stored in `EncryptedSharedPreferences`
- Master key uses AES256_GCM encryption scheme

### Password Hashing
- Passwords are never stored in plain text
- SHA-256 hash is used for credential validation
- Hash format: `SHA256(email:password)`

### Validation
- Minimum password length: 4 characters
- Email must match stored email
- Password hash must match stored hash

## Components

### `OfflineCredentialsManager.kt`
Manages the storage and validation of offline credentials.

**Key Methods:**
- `saveOfflineCredentials(email, password)` - Stores credentials securely
- `validateOfflineCredentials(email, password)` - Validates login attempt
- `hasOfflineCredentials()` - Checks if offline access is set up
- `getStoredEmail()` - Retrieves stored email
- `clearOfflineCredentials()` - Removes all offline credentials

### `OfflineLoginScreen.kt`
UI screen for offline authentication.

**Features:**
- Email and password input fields
- Password visibility toggle
- Offline mode indicator
- Error message display
- Auto-fills stored email if available

### `SetOfflinePasswordScreen.kt`
UI screen for setting up offline password after Auth0 login.

**Features:**
- Password and confirm password fields
- Password visibility toggles
- Real-time validation
- Minimum 4 characters requirement
- Skip option (can be set up later)

### `ReLoginPromptDialog.kt`
Dialog that prompts users to re-authenticate when connection is restored.

**Features:**
- Connection restored indicator
- Log in now button
- Dismiss/Later option

### `NoOfflineAccessScreen.kt`
Informational screen shown when offline access is not set up.

**Features:**
- Clear messaging about offline access requirement
- Instructions to set up offline access

## Usage Flow Diagrams

### First Login Flow
```
User opens app (Online)
    ↓
No stored Auth0 credentials
    ↓
Shows LoginScreen
    ↓
User clicks "Log In" → Auth0 OAuth flow
    ↓
Auth0 login successful
    ↓
No offline password set up yet
    ↓
Shows SetOfflinePasswordScreen
    ↓
User sets offline password (or skips)
    ↓
Shows main app
```

### Offline Login Flow
```
User opens app (Offline)
    ↓
No active Auth0 session
    ↓
Checks if offline credentials exist
    ↓
Yes → Shows OfflineLoginScreen
    ↓
User enters email + offline password
    ↓
Validates against stored hash
    ↓
Valid → User enters app in offline mode
Invalid → Shows error message
```

### Connection Restored Flow
```
User is in app (Offline mode)
    ↓
Internet connection restored
    ↓
Shows ReLoginPromptDialog
    ↓
User clicks "Log In Now"
    ↓
Logs out offline session
    ↓
Shows LoginScreen → Auth0 OAuth flow
    ↓
User re-authenticates
    ↓
Back to full online mode
```

## Configuration

No additional configuration is required. The feature is automatically enabled when the app is built.

## Dependencies

Added to `app/build.gradle.kts`:
```kotlin
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

## Limitations

1. **Offline password is separate from Auth0 password**: Users must remember a different password for offline access (this is by design, as Auth0's OAuth flow doesn't expose the actual password)

2. **Limited functionality in offline mode**: Some features that require server communication won't work in offline mode (e.g., real-time data sync)

3. **Single user offline access**: Only the last user who logged in can use offline mode

4. **Re-authentication required**: When connection is restored, users are prompted to re-authenticate for security and sync purposes

## Future Enhancements

Potential improvements for future versions:

1. Biometric authentication for offline login
2. Auto-sync data when connection is restored
3. Multiple user offline profiles
4. Offline session expiration
5. Settings screen option to change offline password
6. Admin-controlled offline access policies

## Testing

### Test Scenarios

1. **First login with offline setup**
   - Log in via Auth0
   - Verify SetOfflinePasswordScreen appears
   - Set offline password
   - Verify password is stored

2. **Offline login with valid credentials**
   - Turn off internet
   - Open app
   - Enter correct email and offline password
   - Verify app loads in offline mode

3. **Offline login with invalid credentials**
   - Turn off internet
   - Open app
   - Enter wrong password
   - Verify error message appears

4. **Connection restored prompt**
   - Log in offline
   - Turn on internet
   - Verify ReLoginPromptDialog appears
   - Test both "Log In Now" and "Later" options

5. **No offline credentials**
   - Clear app data
   - Turn off internet
   - Open app
   - Verify NoOfflineAccessScreen appears

## Troubleshooting

### User can't log in offline
- Verify offline credentials were set up during an online session
- Check that the email matches exactly
- Ensure the offline password is correct (case-sensitive)
- If forgotten, user must connect to internet and log in via Auth0

### Re-login prompt not appearing
- Verify app has proper network connectivity permissions
- Check that user was actually in offline mode before connection restored
- Restart app if issue persists

### Encrypted storage errors
- May occur on some older devices
- Clear app data and set up offline access again
- If persistent, device may not support required encryption standards

