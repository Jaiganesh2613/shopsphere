
import { useState, useEffect } from 'react';

function App() {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        // Temporary development token.
        // Replace this with a fresh JWT when testing authenticated requests.
        const token = 'PASTE_FRESH_JWT_HERE';

        fetch('http://localhost:8080/api/products', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch products');
                }

                return response.json();
            })
            .then(data => {
                if (Array.isArray(data.content)) {
                    setProducts(data.content);
                } else {
                    setError('Invalid response received from server');
                }
            })
            .catch(error => {
                console.error('Fetch error:', error);
                setError(error.message);
            })
            .finally(() => {
                setLoading(false);
            });
    }, []);

    return (
        <div>
            <h1>Welcome to ShopSphere</h1>

            {loading && <p>Loading products...</p>}

            {error && <p>{error}</p>}

            {!loading && !error && products.length === 0 && (
                <p>No products available.</p>
            )}

            {!error && products.map(product => (
                <div key={product.id}>
                    <h2>{product.name}</h2>
                    <p>Price: ₹{product.price}</p>
                    <p>Stock: {product.stockQuantity}</p>
                </div>
            ))}
        </div>
    );
}

export default App;
