import './FormCheckbox.css';

export default function FormCheckbox(props) {
  const { label, checked, onChange, name, disabled = false } = props;

  return (
    <label class="form-checkbox-wrapper">
      <input
        type="checkbox"
        class="form-checkbox"
        name={name}
        checked={checked}
        onChange={(e) => onChange(e.target.checked)}
        disabled={disabled}
      />
      <span class="form-checkbox-label">{label}</span>
    </label>
  );
}
