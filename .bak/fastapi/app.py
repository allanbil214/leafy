# app.py
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
from model import plant_disease_model
from pydantic import BaseModel
from weather import WeatherService, LocationData
import os

from dotenv import load_dotenv

# Load environment variables
load_dotenv()

app = FastAPI()

# Initialize the model with the path to the TFLite model file
model_path = "model-fix"  # Replace with your model path
model = plant_disease_model(model_path=model_path)
weather_service = WeatherService(api_key=os.getenv("OPENWEATHER_API_KEY"))

class ImageData(BaseModel):
    base64_encoded: str

@app.get("/")
def read_root():
    return {"message": "Welcome to the Plant Disease Prediction API"}

@app.post("/predict")
async def predict_plant_disease(image_data: ImageData):
    try:
        # Make a prediction using the base64 encoded image data
        base64_encoded = image_data.base64_encoded
        prediction_result = model.predict_tf(base64_encoded)
        plant, disease, url = model.split_class_name(prediction_result["class_name"])
        
        print(prediction_result)

        # Return results
        return JSONResponse(content={
            "predicted_class": prediction_result["class_name"],
            "plant_name": plant,
            "plant_disease": disease,
            "plant_url": url,
            "confidence": prediction_result["confidence"]
        })

    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/disease-info/{disease}")
def get_disease_info(disease: str):
    try:
        # Determine the response based on the predicted class
        if "healthy" in disease:
            info = plant_disease_model.prompt_healthy(disease)
        elif "unknown" in disease:
            info = plant_disease_model.prompt_healthy()
        else:
            info = plant_disease_model.prompt_disease(disease)
        return JSONResponse(content={"disease_info": info})
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/weather/current")
async def get_current_weather(location: LocationData):
    """Get current weather for the given location."""
    try:
        weather_data = weather_service.get_current_weather(
            lat=location.latitude,
            lon=location.longitude
        )
        return weather_service.parse_weather_data(weather_data)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/weather/forecast")
async def get_weather_forecast(location: LocationData):
    """Get 5-day weather forecast for the given location."""
    try:
        forecast_data = weather_service.get_weather_forecast(
            lat=location.latitude,
            lon=location.longitude
        )
        return JSONResponse(content=forecast_data)
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="localhost", port=8000)

# http://localhost:8000/docs

# ngrok tunnel --label edge=edghts_2hSDHwV7XJW9QAs98aVHqzxZItA http://localhost:8000

# ngrok tunnel --label edge=edghts_2hSDHwV7XJW9QAs98aVHqzxZItA http://127.0.0.1:8000/