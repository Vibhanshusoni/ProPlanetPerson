// 1. Load Environment Variables First
// This ensures our sensitive data is available before anything else
require('dotenv').config();

// 2. Import Required Libraries
const express = require('express'); // Our web framework
const mongoose = require('mongoose'); // For MongoDB interaction
const app = express(); // Initialize Express application

// 3. Middleware - Order matters!
// express.json() is built-in middleware to parse JSON request bodies
// This allows us to receive JSON data from the Android app (e.g., login credentials)
app.use(express.json());

// 4. Database Connection
const connectDB = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI, {
            // These options are deprecated and can be removed
            // useNewUrlParser: true,  <-- REMOVE THIS LINE
            // useUnifiedTopology: true, <-- REMOVE THIS LINE
        });
        console.log('MongoDB Connected...');
    } catch (err) {
        console.error('MongoDB Connection Error:', err.message);
        process.exit(1);
    }
};

// Call the database connection function
connectDB();

// ... (previous code in app.js)

// 5. Define a Basic Root Route (for testing if server is running)
app.get('/', (req, res) => {
    res.send('API is running...');
});

// 6. Define Routes (Authentication routes will go here later)
// This line connects our authRoutes to the /api/auth path
app.use('/api/auth', require('./routes/authRoutes'));

// app.use('/api/products', require('./routes/productRoutes')); // Will add later

// 7. Start the Server
// Use the PORT from .env, or default to 5000
const PORT = process.env.PORT || 5000;

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));