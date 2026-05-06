import Header from '../../components/Header/Header';
import Footer from '../../components/Footer/Footer';
import RestReminder from '../RestReminder/RestReminder';

export default function Layout(props) {
  return (
    <>
      <Header />
      <main class="main-content">
        {props.children}
      </main>
      <Footer />
      <RestReminder />
    </>
  );
}
