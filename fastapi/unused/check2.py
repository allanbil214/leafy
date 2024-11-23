import requests

API_KEY = "ca95221c5e1986a65636b155d53db2be"
BASE_URL = "https://api.openweathermap.org/data/2.5/weather"

# Parameters
city = "Yogyakarta"
params = {
    "q": city,
    "appid": API_KEY,
    "units": "metric"  # Optional: Use "imperial" for Fahrenheit
}

# Make the request
response = requests.get(BASE_URL, params=params)

# Check response
if response.status_code == 200:
    data = response.json()
    print(f"City: {data['name']}")
    print(f"Temperature: {data['main']['temp']}°C")
    print(f"Weather: {data['weather'][0]['description']}")
else:
    print(f"Error: {response.status_code}, {response.text}")
