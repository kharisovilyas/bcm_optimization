function runOptimization() {
    const input = {
        t1Range: {
            min: parseFloat(document.getElementById('t1Min').value),
            max: parseFloat(document.getElementById('t1Max').value)
        },
        t2Range: {
            min: parseFloat(document.getElementById('t2Min').value),
            max: parseFloat(document.getElementById('t2Max').value)
        }
    };

    fetch('/api/v1/optimize', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(input)
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('result').innerHTML =
            `Optimal T1: ${data.t1.toFixed(2)}°C, T2: ${data.t2.toFixed(2)}°C, Objective Value: ${data.objectiveValue.toFixed(2)}`;
        plotSurface();
    })
    .catch(error => console.error('Error:', error));
}

function plotSurface() {
    const t1 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const t2 = Array.from({length: 50}, (_, i) => -3 + (14 - (-3)) * i / 49);
    const z = t1.map(t1 => t2.map(t2 => {
        if (t1 + t2 > 12) return null;
        return 8 * (Math.pow(t2 - t1, 2) + (1/9) * Math.pow(t1 + t2 - 10, 2));
    }));

    const data = [{
        x: t1,
        y: t2,
        z: z,
        type: 'surface'
    }];

    Plotly.newPlot('surfacePlot', data, {
        title: 'Objective Function Surface',
        scene: {
            xaxis: {title: 'T1 (°C)'},
            yaxis: {title: 'T2 (°C)'},
            zaxis: {title: 'Objective Value'}
        }
    });
}