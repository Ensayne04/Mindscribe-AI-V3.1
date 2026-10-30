<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Task & Focus Workspace

Welcome to Task & Focus Workspace! This application is designed to help you manage your tasks efficiently while ensuring your data remains secure and private.

With built-in features for journaling, task planning, and a dedicated dashboard, you can keep all your thoughts and tasks in one place. We prioritize your privacy with local inference and an app lock to keep your sensitive data safe.

## Features
- **Task Management:** Add, prioritize, and manage tasks easily.
- **Private Journal:** Keep track of your daily thoughts with our built-in journal that supports secure, local AI processing.
- **Security:** Use PIN and Biometric App Lock to secure your workspace.
- **Data Export:** Export and backup your data explicitly when needed.

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)

1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

## Support the Project

If you find this application useful and would like to support its development, consider buying me a coffee! ☕

<a href="https://www.buymeacoffee.com/yourusername" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>
