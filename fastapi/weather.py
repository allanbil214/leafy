# weather.py
from dataclasses import dataclass
from datetime import datetime
import requests
from typing import List, Optional
from pydantic import BaseModel

# Weather-related models
class WeatherCoordinates(BaseModel):
    lat: float
    lon: float

class WeatherMain(BaseModel):
    temp: float
    feels_like: float
    temp_min: float
    temp_max: float
    pressure: int
    humidity: int

class WeatherDescription(BaseModel):
    id: int
    main: str
    description: str
    icon: str

class WindInfo(BaseModel):
    speed: float
    deg: int
    gust: Optional[float] = None

class WeatherResponse(BaseModel):
    coord: WeatherCoordinates
    weather: List[WeatherDescription]
    main: WeatherMain
    visibility: int
    wind: WindInfo
    dt: int
    name: str
    
class LocationData(BaseModel):
    latitude: float
    longitude: float

class WeatherService:
    def __init__(self, api_key: str):
        self.api_key = api_key
        self.base_url = "http://api.openweathermap.org/data/2.5"

    def get_current_weather(self, lat: float, lon: float) -> WeatherResponse:
        """Get current weather data for given coordinates."""
        endpoint = f"{self.base_url}/weather"
        params = {
            "lat": lat,
            "lon": lon,
            "appid": self.api_key,
            "units": "metric"  # Use metric units
        }
        
        response = requests.get(endpoint, params=params)
        response.raise_for_status()
        return WeatherResponse(**response.json())

    def get_weather_forecast(self, lat: float, lon: float) -> dict:
        """Get 5-day weather forecast for given coordinates."""
        endpoint = f"{self.base_url}/forecast"
        params = {
            "lat": lat,
            "lon": lon,
            "appid": self.api_key,
            "units": "metric"
        }
        
        response = requests.get(endpoint, params=params)
        response.raise_for_status()
        return response.json()

    def parse_weather_data(self, weather_data: WeatherResponse) -> dict:
        """Parse weather data into a more user-friendly format."""
        return {
            "location": weather_data.name,
            "temperature": {
                "current": round(weather_data.main.temp, 1),
                "feels_like": round(weather_data.main.feels_like, 1),
                "min": round(weather_data.main.temp_min, 1),
                "max": round(weather_data.main.temp_max, 1)
            },
            "weather": {
                "main": weather_data.weather[0].main,
                "description": weather_data.weather[0].description.capitalize(),
                "icon": f"https://openweathermap.org/img/wn/{weather_data.weather[0].icon}@2x.png"
            },
            "humidity": weather_data.main.humidity,
            "wind": {
                "speed": round(weather_data.wind.speed * 3.6, 1),  # Convert m/s to km/h
                "direction": weather_data.wind.deg
            },
            "timestamp": datetime.fromtimestamp(weather_data.dt).isoformat()
        }