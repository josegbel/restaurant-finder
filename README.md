# TripAdvisor Restaurant Finder 🍽️

Welcome to **TripAdvisor Restaurant Finder**, an innovative mobile application designed to enhance your dining experiences. This app allows you to search for nearby restaurants, query specific dining spots, add your favorite restaurants to a Room database, and view detailed information about your saved restaurants in a details screen. Built with Jetpack Compose and the Navigation library, and using Retrofit for network communication, **TripAdvisor Restaurant Finder** offers a seamless and intuitive user experience. Whether you're planning your next meal out or exploring dining options in a new city, our app has got you covered.

## Getting Started 🚀

To get started with developing or building **TripAdvisor Restaurant Finder**, follow the steps below to set up your environment.

### Prerequisites

Ensure you have the following installed:
- Android Studio (latest version recommended)
- An Android device or emulator running Android API level 21 (Lollipop) or higher

### Setup

#### Clone the Repository

Clone the repository to your local machine using Git:

```bash
git clone git@github.com:josegbel/restaurantfinder.git
```

Navigate into the project directory:

```bash
cd restaurantfinder
```

#### TripAdvisor API Key

To fetch information about restaurants, **TripAdvisor Restaurant Finder** utilizes the TripAdvisor API. A valid API key is required to make successful requests to this API.

##### How to Obtain an API Key

1. Visit the TripAdvisor API Developer Portal: [https://developer-tripadvisor.com/](https://developer-tripadvisor.com/)
2. Sign up for an account or log in if you already have one.
3. Follow the portal's instructions to create a new app and obtain your API key.

##### Adding Your API Key to the Project

After obtaining your API key, add it to your project to enable API requests:

1. Open the `local.properties` file located in the root directory of your project.
2. Add a new line at the end of the file with your API key:

```properties
tripadvisor_api_key="YOUR_API_KEY_HERE"
```

Replace `YOUR_API_KEY_HERE` with the actual API key you obtained.

### Building and Running the App 🏗️

With the API key in place, you're ready to build and run the app:

1. Open the project in Android Studio.
2. Select a device or emulator.
3. Hit the 'Run' button.

The app will build and install on your selected device or emulator.

## Features 🌟

- **Search for Nearby Restaurants**: Easily find dining options around you.
- **Query Restaurants**: Look up specific restaurants based on your preferences.
- **Favorite Restaurants**: Save your top picks to a Room database for easy access.
- **Details Screen**: View detailed information about your saved restaurants.
- **Modern Tech Stack**: Built with Jetpack Compose, Navigation library, Retrofit, JUnit, and Espresso.

## Contributing 🤝

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

## License 📄

**TripAdvisor Restaurant Finder** is licensed under the MIT License. See the `LICENSE` file for more details.