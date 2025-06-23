const mongoose = require('mongoose');
const bcrypt = require('bcryptjs'); // For password hashing

// Define the User Schema
const UserSchema = new mongoose.Schema({
    email: {
        type: String,
        required: true,
        unique: true, // Ensures email is unique for each user
        lowercase: true, // Stores email in lowercase
        match: [/.+@.+\..+/, 'Please fill a valid email address'] // Basic email regex validation
    },
    password: {
        type: String,
        required: true,
        minlength: 6 // Minimum password length
    },
    role: {
        type: String,
        enum: ['buyer', 'seller', 'admin'], // Allowed roles for users
        default: 'buyer' // New users will default to 'buyer'
    },
    createdAt: {
        type: Date,
        default: Date.now // Automatically set creation date
    }
});

// Middleware to hash password before saving (pre-save hook)
// 'pre' means it runs before the 'save' event
UserSchema.pre('save', async function(next) {
    // Only hash the password if it has been modified (or is new)
    if (!this.isModified('password')) {
        return next();
    }

    try {
        // Generate a salt (random value used to hash password)
        const salt = await bcrypt.genSalt(10); // 10 is the number of rounds, a good balance between security and speed
        // Hash the password using the generated salt
        this.password = await bcrypt.hash(this.password, salt);
        next(); // Continue with the save operation
    } catch (err) {
        next(err); // Pass error to the next middleware/error handler
    }
});

// Method to compare entered password with hashed password
// This will be used during login
UserSchema.methods.matchPassword = async function(enteredPassword) {
    // bcrypt.compare returns true if passwords match, false otherwise
    return await bcrypt.compare(enteredPassword, this.password);
};

// Create and export the User Model
module.exports = mongoose.model('User', UserSchema);