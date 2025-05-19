const configs = [
    { alpha: 0.5, beta: 0.7, gamma: 0.9 },
    { alpha: 0.6, beta: 0.8, gamma: 1.0 },
    { alpha: 0.7, beta: 0.9, gamma: 1.1 },
    { alpha: 0.8, beta: 1.0, gamma: 1.2 },
    { alpha: 0.9, beta: 1.1, gamma: 1.3 },
    { alpha: 1.0, beta: 1.2, gamma: 1.4 },
    { alpha: 1.1, beta: 1.3, gamma: 1.5 },
    { alpha: 1.2, beta: 0.5, gamma: 0.7 },
    { alpha: 1.3, beta: 0.6, gamma: 0.8 },
    { alpha: 1.4, beta: 0.7, gamma: 0.9 }
];

function updateCoefficients() {
    const configIndex = document.getElementById('configSelect').value;
    const config = configs[configIndex];

    // Обновляем α
    const alphaElement = document.getElementById('alphaCoefficient');
    alphaElement.classList.remove('fade-in');
    void alphaElement.offsetWidth;
    alphaElement.innerHTML = `α=${config.alpha}`;
    alphaElement.classList.add('fade-in');

    // Обновляем β
    const betaElement = document.getElementById('betaCoefficient');
    betaElement.classList.remove('fade-in');
    void betaElement.offsetWidth;
    betaElement.innerHTML = `β=${config.beta}`;
    betaElement.classList.add('fade-in');

    // Обновляем γ
    const gammaElement = document.getElementById('gammaCoefficient');
    gammaElement.classList.remove('fade-in');
    void gammaElement.offsetWidth;
    gammaElement.innerHTML = `γ=${config.gamma}`;
    gammaElement.classList.add('fade-in');
}

function runOptimization() {
    const configIndex = document.getElementById('configSelect').value;
    const config = configs[configIndex];
    const input = {
        t1Range: {
            min: parseFloat(document.getElementById('t1Min').value),
            max: parseFloat(document.getElementById('t1Max').value)
        },
        t2Range: {
            min: parseFloat(document.getElementById('t2Min').value),
            max: parseFloat(document.getElementById('t2Max').value)
        },
        method: document.getElementById('methodSelect').value,
        alpha: config.alpha,
        beta: config.beta,
        gamma: config.gamma
    };

    fetch('/api/optimize', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(input)
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('result').innerHTML =
            `Оптимальное T₁: ${data.t1.toFixed(2)}°C, T₂: ${data.t2.toFixed(2)}°C, Значение функции: ${data.objectiveValue.toFixed(2)}`;
        plotSurface();
        plotContour();
    })
    .catch(error => console.error('Ошибка:', error));
}

function plotSurface() {
    const configIndex = document.getElementById('configSelect').value;
    const config = configs[configIndex];
    const t1 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const t2 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const z = t1.map(t1 => t2.map(t2 => {
        if (t1 + t2 > 12) return null;
        return config.alpha * 8 * (config.beta * Math.pow(t2 - t1, 2) + config.gamma * (1/9) * Math.pow(t1 + t2 - 10, 2));
    }));

    const data = [{
        x: t1,
        y: t2,
        z: z,
        type: 'surface',
        colorscale: 'Viridis'
    }];

    Plotly.newPlot('surfacePlot', data, {
        title: 'Поверхность отклика целевой функции',
        scene: {
            xaxis: {title: 'T₁ (°C)'},
            yaxis: {title: 'T₂ (°C)'},
            zaxis: {title: 'f(T₁, T₂)'}
        }
    });
}

function plotContour() {
    const configIndex = document.getElementById('configSelect').value;
    const config = configs[configIndex];
    const t1 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const t2 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const z = t1.map(t1 => t2.map(t2 => {
        if (t1 + t2 > 12) return null;
        return config.alpha * 8 * (config.beta * Math.pow(t2 - t1, 2) + config.gamma * (1/9) * Math.pow(t1 + t2 - 10, 2));
    }));

    const data = [{
        x: t1,
        y: t2,
        z: z,
        type: 'contour',
        contours: {
            coloring: 'fill',
            showlabels: true,
            labelfont: { family: 'Roboto', size: 12, color: '#222B45' }
        },
        colorscale: 'Viridis'
    }];

    Plotly.newPlot('contourPlot', data, {
        title: 'f(T₁, T₂) — Линии равного значения',
        xaxis: {title: 'T₁ (°C)'},
        yaxis: {title: 'T₂ (°C)'}
    });
}

// Инициализация коэффициентов и графиков при загрузке страницы
updateCoefficients();
plotSurface();
plotContour();