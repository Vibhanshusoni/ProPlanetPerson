const express = require('express');
const router = express.Router(); // Initialize Express Router
const User = require('../models/User'); // Import our User model
const jwt = require('jsonwebtoken'); // For generating JWTs

// Helper function to generate JWT
// This function will be called whenever a user successfully registers or logs in
const generateToken = (id, role) => {
    return jwt.sign(
        { id, role }, // Payload: user ID and user role
        process.env.JWT_SECRET, // Secret key from our .env file
        { expiresIn: '1h' } // Token expires in 1 hour
    );
};

/**
 * @route   POST /api/auth/register
 * @desc    Register a new user
 * @access  Public
 */
router.post('/register', async (req, res) => {
    const { email, password } = req.body; // Extract email and password from request body

    // Basic validation
    if (!email || !password) {
        return res.status(400).json({ success: false, message: 'Please enter all fields' });
    }

    try {
        // Check if user already exists
        let user = await User.findOne({ email });
        if (user) {
            return res.status(400).json({ success: false, message: 'User with that email already exists' });
        }

        // Create a new user instance. The 'pre' save hook in User.js will hash the password.
        user = new User({
            email,
            password,
            role: 'buyer' // Default role for new registrations
        });

        await user.save(); // Save the user to the database

        // Generate a token for the newly registered user (for immediate login)
        const token = generateToken(user._id, user.role);

        res.status(201).json({
            success: true,
            message: 'User registered successfully',
            token,
            user_id: user._id,
            user_role: user.role // Send back the role so Android app knows which dashboard to go to
        });

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: 'Server error during registration' });
    }
});

/**
 * @route   POST /api/auth/login
 * @desc    Authenticate user & get token
 * @access  Public
 */
router.post('/login', async (req, res) => {
    const { email, password } = req.body; // Extract email and password

    // Basic validation
    if (!email || !password) {
        return res.status(400).json({ success: false, message: 'Please enter all fields' });
    }

    try {
        // Find user by email
        const user = await User.findOne({ email });
        if (!user) {
            return res.status(400).json({ success: false, message: 'Invalid credentials' });
        }

        // Compare provided password with hashed password using the method defined in User.js
        const isMatch = await user.matchPassword(password);
        if (!isMatch) {
            return res.status(400).json({ success: false, message: 'Invalid credentials' });
        }

        // If credentials are valid, generate a token
        const token = generateToken(user._id, user.role);

        res.json({
            success: true,
            message: 'Logged in successfully',
            token,
            user_id: user._id,
            user_role: user.role // Send back the role for client-side routing
        });

    } catch (err) {
        console.error(err.message);
        res.status(500).json({ success: false, message: 'Server error during login' });
    }
});

module.exports = router; // Export the router to be used in app.js